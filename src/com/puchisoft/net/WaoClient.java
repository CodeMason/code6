package com.puchisoft.net;

import java.io.IOException;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.WalkingAroundOnline;
import com.puchisoft.net.Network.LogMessage;
import com.puchisoft.net.Network.RegisterName;

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
				RegisterName registerName = new RegisterName();
				registerName.name = "USER"+Math.random();
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
			LogMessage cc = (LogMessage) message;
			game.setStatus(cc.message);
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