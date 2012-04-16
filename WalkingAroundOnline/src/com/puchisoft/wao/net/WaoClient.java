package com.puchisoft.wao.net;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.wao.GameMap;
import com.puchisoft.wao.net.Network.LogMessage;
import com.puchisoft.wao.net.Network.Login;
import com.puchisoft.wao.net.Network.MovementChange;
import com.puchisoft.wao.net.Network.PlayerJoinLeave;

public class WaoClient {
	
	private Client client;
	private GameMap map;
	public int id;
	public String remoteIP;

	public WaoClient(final GameMap game) { //
		this.map = game;
		
		client = new Client();
		client.start();

		// For consistency, the classes to be sent over the network are
		// registered by the same method for both the client and server.
		Network.register(client);

		client.addListener(new Listener() {
			public void connected (Connection connection) {
				handleConnect(connection);
			}

			public void received (Connection connection, Object object) {
				handleMessage(connection.getID(), object);
			}

			public void disconnected (Connection connection) {
				handleDisonnect(connection);
			}
		});

	}
		

	protected void handleDisonnect(Connection connection) {
		map.onDisconnect();
	}


	protected void handleConnect(Connection connection) {
		id = connection.getID();
		remoteIP = connection.getRemoteAddressTCP().toString();
		Login registerName = new Login("USER"+Math.random(), Network.version);
		client.sendTCP(registerName);
		client.updateReturnTripTime();
		map.setNetworkClient(this);
	}


	public void connectLocal() {
		connect("localhost", Network.port);
	}
	
	public void connect(String host, int port) {
		try {
			client.connect(5000, host, port);
		} catch (IOException e) {
//			e.printStackTrace();
			map.setStatus("Can't connect to "+host);
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
			map.setStatus(msg.message);
		}
		else if(message instanceof PlayerJoinLeave){
			PlayerJoinLeave msg = (PlayerJoinLeave) message;
			if(msg.hasJoined){
				map.setStatus(msg.name + " joined");
				map.addPlayer(msg);
			}
			else{
				map.setStatus(msg.name + " left");
				map.removePlayer(msg);
			}
		}
		else if(message instanceof MovementChange){
			MovementChange msg = (MovementChange) message;
			map.playerMoved(msg);
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
