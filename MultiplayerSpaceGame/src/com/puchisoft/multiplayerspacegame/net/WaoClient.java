package com.puchisoft.multiplayerspacegame.net;

import java.io.IOException;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.multiplayerspacegame.GameMap;
import com.puchisoft.multiplayerspacegame.net.Network.GameMapData;
import com.puchisoft.multiplayerspacegame.net.Network.Login;
import com.puchisoft.multiplayerspacegame.net.Network.MovementChange;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerJoinLeave;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerShoots;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerWasHit;

public class WaoClient {

	private Client client;
	private GameMap map;
	public int id;
	private String name;
	public String remoteIP;
	
	private Random random = new Random();

	public WaoClient(final GameMap game, String name) { //
		this.map = game;
		this.name = name;

		client = new Client();
		client.start();
//		InetAddress found = client.discoverHost(Network.portUdp, 5000);
//		System.out.println(found.toString());

		// For consistency, the classes to be sent over the network are
		// registered by the same method for both the client and server.
		Network.register(client);

		client.addListener(new Listener() {
			public void connected(Connection connection) {
				handleConnect(connection);
			}

			public void received(Connection connection, Object object) {
				handleMessage(connection.getID(), object);
			}

			public void disconnected(Connection connection) {
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
		Color color = new Color(random.nextFloat()*0.5f+0.5f,random.nextFloat()*0.5f+0.5f,random.nextFloat()*0.5f+0.5f,1);
		Login registerName = new Login(name, Network.version, color);
		client.sendTCP(registerName);
		client.updateReturnTripTime();
		map.onConnect(this,name,color);
	}

	public void connectLocal() {
		connect("localhost");
	}

	public void connect(String host) {
		try {
			client.connect(5000, host, Network.port);//, Network.portUdp);
		} catch (IOException e) {
			// e.printStackTrace();
			map.setStatus("Can't connect to " + host);
			Log.error("Can't connect to " + host);
		}
	}

	public void tick() {
		// nothing to do
	}

	public void sendMessage(Object message) {
		Log.info("SENT packet");
		if (client.isConnected()) {
			client.sendTCP(message);
		}
	}

	public void handleMessage(int playerId, Object message) {

//		if (message instanceof LogMessage) {
//			LogMessage msg = (LogMessage) message;
//			map.setStatus(msg.message);
//		} else 
		if (message instanceof PlayerJoinLeave) {
			PlayerJoinLeave msg = (PlayerJoinLeave) message;
			if (msg.hasJoined) {
				map.setStatus(msg.name + " joined");
				map.addPlayer(msg);
			} else {
				map.setStatus(msg.name + " left");
				map.removePlayer(msg);
			}
		} else if (message instanceof MovementChange) {
			MovementChange msg = (MovementChange) message;
			map.playerMoved(msg);
		} else if (message instanceof PlayerShoots) {
			PlayerShoots msg = (PlayerShoots) message;
			map.addBullet(msg);
		} else if (message instanceof GameMapData) {
			GameMapData msg = (GameMapData) message;
			map.setStateData(msg);
		} else if (message instanceof PlayerWasHit) {
			PlayerWasHit msg = (PlayerWasHit) message;
			map.onMsgPlayerWasHit(msg);
		}

	}

	public void ping() {
		if (client.isConnected()) {
			this.client.updateReturnTripTime();
		}
	}

	public void shutdown() {
		client.stop();
		client.close();
	}

}
