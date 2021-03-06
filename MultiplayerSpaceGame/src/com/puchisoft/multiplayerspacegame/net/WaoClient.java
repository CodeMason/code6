package com.puchisoft.multiplayerspacegame.net;

import java.io.IOException;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.multiplayerspacegame.GameMap;
import com.puchisoft.multiplayerspacegame.net.Network.AsteroidData;
import com.puchisoft.multiplayerspacegame.net.Network.AsteroidWasHit;
import com.puchisoft.multiplayerspacegame.net.Network.GameMapData;
import com.puchisoft.multiplayerspacegame.net.Network.Login;
import com.puchisoft.multiplayerspacegame.net.Network.MovementState;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerJoinLeave;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerShoots;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerSpawns;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerWasHit;
import com.puchisoft.multiplayerspacegame.net.Network.RoundEnd;
import com.puchisoft.multiplayerspacegame.net.Network.RoundStart;

public class WaoClient {

	private Client client;
	private GameMap map;
	public int id;
	private String name;
	public String remoteIP;
	
	private Random random = new Random();

	public WaoClient(String name) { //final GameMap game,
		this.map = new GameMap(this);
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
		map.onConnect(name,color);
	}

	public void connectLocal() {
		connect("localhost");
	}

	public void connect(String host) {
		try {
			client.connect(5000, host, Network.port, Network.portUdp);//, Network.portUdp);
		} catch (IOException e) {
			// e.printStackTrace();
			map.setStatus(!host.equals("localhost") ? "Can't connect to " + host +". Hit ESC." : "You didn't enter an IP to connect to. Hit ESC and try again.");
			logInfo("Can't connect to " + host);
		}
	}

	private void logInfo(String string) {
		Log.info(string);
	}

	public void tick() {
		// nothing to do
	}

	public void sendMessage(Object message) {
		map.logInfo("SENT packet TCP");
		if (client.isConnected()) {
			client.sendTCP(message);
		}
	}
	
	public void sendMessageUDP(Object message) {
		map.logInfo("SENT packet UPD");
		if (client.isConnected()) {
			client.sendUDP(message);
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
		} else if (message instanceof MovementState) {
			MovementState msg = (MovementState) message;
			map.playerMoved(msg);
		} else if (message instanceof PlayerShoots) {
			PlayerShoots msg = (PlayerShoots) message;
			map.onMsgPlayerShoots(msg);
		} else if (message instanceof PlayerWasHit) {
			PlayerWasHit msg = (PlayerWasHit) message;
			map.onPlayerWasHit(msg);
		} else if (message instanceof GameMapData) {
			GameMapData msg = (GameMapData) message;
			map.setStateData(msg);
		} else if (message instanceof AsteroidData) {
			AsteroidData msg = (AsteroidData) message;
			map.addAsteroid(msg);
		} else if (message instanceof AsteroidWasHit) {
			AsteroidWasHit msg = (AsteroidWasHit) message;
			map.removeAsteroid(msg.position);
		} else if (message instanceof RoundEnd) {
			RoundEnd msg = (RoundEnd) message;
			map.onRoundEnd(msg);
		} else if (message instanceof RoundStart) {
			RoundStart msg = (RoundStart) message;
			map.onRoundStart(msg);
		} else if (message instanceof PlayerSpawns) {
			PlayerSpawns msg = (PlayerSpawns) message;
			map.onPlayerSpawn(msg);
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

	public GameMap getMap() {
		return this.map;
	}

}
