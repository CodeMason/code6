package com.puchisoft.net;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.WalkingAroundOnline;
import com.puchisoft.net.Network.LogMessage;
import com.puchisoft.net.Network.Login;
import com.puchisoft.net.Network.MovementChange;
import com.puchisoft.net.Network.PlayerJoinLeave;

public class WaoClient {
	
	private Client client;
	private WalkingAroundOnline game;

	public WaoClient(final WalkingAroundOnline game) {
		this.game = game;
		
		client = new Client();
		client.start();

		// For consistency, the classes to be sent over the network are
		// registered by the same method for both the client and server.
		Network.register(client);

		client.addListener(new Listener() {
			public void connected (Connection connection) {
				game.setStatus("Connected to "+connection.getRemoteAddressTCP());
				game.setNetworkId(connection.getID());
				Login registerName = new Login("USER"+Math.random(), Network.version);
				client.sendTCP(registerName);
				client.updateReturnTripTime();
			}

			public void received (Connection connection, Object object) {
				handleMessage(connection.getID(), object);
			}

			public void disconnected (Connection connection) {
				game.setStatus("Disconnected");
			}
		});

	}
	

	public void connectLocal() {
		connect("localhost", Network.port);
	}
	
	public void connect(String host, int port) {
		try {
			client.connect(5000, host, port);
		} catch (IOException e) {
//			e.printStackTrace();
			game.setStatus("Can't connect to "+host);
			Log.error("Can't connect to "+host);
		}
	}

	public void tick() {
		// nothing to do
	}

	public void sendMessage(Object message) {
		if (client.isConnected()) {
			client.sendTCP(message);
		}
	}

	public void handleMessage(int playerId, Object message) {

		if (message instanceof LogMessage) {
			LogMessage msg = (LogMessage) message;
			game.setStatus(msg.message);
		}
		else if(message instanceof PlayerJoinLeave){
			PlayerJoinLeave msg = (PlayerJoinLeave) message;
			if(msg.hasJoined){
				game.setStatus(msg.name + " joined");
				game.addPlayer(msg);
			}
			else{
				game.setStatus(msg.name + " left");
				game.removePlayer(msg);
			}
		}
		else if(message instanceof MovementChange){
			MovementChange msg = (MovementChange) message;
			game.playerMoved(msg);
		}

	}

	public void ping() {
		if (client.isConnected()) {
			this.client.updateReturnTripTime();		
		}
	}

	public void shutdown() {
		client.close();
	}

}
