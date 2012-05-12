package com.puchisoft.multiplayerspacegame.net;

import java.io.IOException;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.multiplayerspacegame.GameMap;
import com.puchisoft.multiplayerspacegame.net.Network.AsteroidWasHit;
import com.puchisoft.multiplayerspacegame.net.Network.GameMapData;
import com.puchisoft.multiplayerspacegame.net.Network.Login;
import com.puchisoft.multiplayerspacegame.net.Network.MovementChange;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerJoinLeave;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerShoots;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerWasHit;

public class WaoServer {
	Server server;
	private GameMap map;
	private Random random = new Random();

	public WaoServer() throws IOException {
		//Log.set(Log.LEVEL_DEBUG);
		map = new GameMap(this);
		map.generateMap(100);
		
		server = new Server() {
			protected Connection newConnection() {
				// By providing our own connection implementation, we can store
				// per
				// connection state without a connection ID to state look up.
				return new WaoConnection();
			}
		};

		// For consistency, the classes to be sent over the network are
		// registered by the same method for both the client and server.
		Network.register(server);

		server.addListener(new Listener() {
			public void received(Connection c, Object message) {
				// We know all connections for this server are actually WaoConnection
				WaoConnection connection = (WaoConnection) c;
				
				if (message instanceof Login) {
					Login msg = ((Login)message);
					// Ignore the object if a client has already registered a name. This is
					// impossible with our client, but a hacker could send messages at any time.
					if (connection.name != null) return;
					// Ignore the object if the name is invalid.
					String name = msg.name;
					if (name == null) return;
					name = name.trim();
					if (name.length() == 0) return;
					// Store the name on the connection.
					connection.name = name;
					connection.color = msg.color;
					if(msg.version != Network.version){
						Log.error("wrong version");
						connection.close();
					}else{
						// Tell old people about new person
						PlayerJoinLeave reply  = new PlayerJoinLeave(connection.getID(), connection.name, true, connection.color, connection.score);
						server.sendToAllExceptTCP(connection.getID(), reply);
						
						// Tell new person about asteroids
						connection.sendTCP(map.getStateData());
						
						// Tell new person about old people
						for(Connection con: server.getConnections()){
							WaoConnection conn = (WaoConnection)con;
							if(conn.getID() != connection.getID() && conn.name != null){ // Not self, Have logged in
								PlayerJoinLeave hereMsg  = new PlayerJoinLeave(conn.getID(), conn.name, true, conn.color, conn.score);
								connection.sendTCP(hereMsg);
							}
						}
					}
					return;
				}
				else if(message instanceof MovementChange) {
					MovementChange msg = (MovementChange)message;
					msg.playerId = connection.getID();
					// TODO Remember more about player movement state, compute changes
					connection.position = msg.position;
					server.sendToAllExceptTCP(connection.getID(), msg);
				}
				else if(message instanceof PlayerShoots) {
					PlayerShoots msg = (PlayerShoots)message;
					msg.playerID = connection.getID();
					map.addBullet(msg);
					// TODO some sort of validation: remember last shot time or such 
					server.sendToAllExceptTCP(connection.getID(), msg);
				}
				else if(message instanceof PlayerWasHit) {
					PlayerWasHit msg = (PlayerWasHit)message;
					msg.playerIdVictim = connection.getID();
					WaoConnection hitter = getConnectionById(msg.playerIdHitter);
					if(hitter != null){
						hitter.score++;
						Log.info(hitter.name+" "+hitter.getID()+" hit someone");
						server.sendToAllExceptTCP(connection.getID(), msg);
					}
				}
				else if(message instanceof AsteroidWasHit) {
					Log.error("Client tried to send message only server sends");
				}
				
			}
			
			public void disconnected (Connection c) {
				WaoConnection connection = (WaoConnection)c;
				if (connection.name != null) {
					// Announce to everyone that someone has left.
					PlayerJoinLeave reply  = new PlayerJoinLeave(connection.getID(), connection.name, false, connection.color, connection.score);
					server.sendToAllExceptTCP(connection.getID(), reply);
				}
			}
		});
		
		server.bind(Network.port); //,Network.portUdp);
		server.start();
	}
	
	public void update(float delta) {
		map.update(delta);
	}
	
	// Connection specific attributes
	static class WaoConnection extends Connection {
		public String name;
		public Vector2 position;
		protected Color color;
		public int score = 0;
	}
	
	private WaoConnection getConnectionById(int id){
		for(Connection con: server.getConnections()){
			WaoConnection conn = (WaoConnection)con;
			if (conn.getID() == id) return conn;
		}
		return null;
	}

	public void shutdown() {
		server.close();
		server.stop();
	}

	public void sendMessage(Object message) {
		server.sendToAllTCP(message);
	}

}
