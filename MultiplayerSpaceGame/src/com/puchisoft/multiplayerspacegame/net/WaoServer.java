package com.puchisoft.multiplayerspacegame.net;

import java.io.IOException;
import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive;
import com.esotericsoftware.kryonet.FrameworkMessage.Ping;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.multiplayerspacegame.GameMap;
import com.puchisoft.multiplayerspacegame.Player;
import com.puchisoft.multiplayerspacegame.net.Network.Login;
import com.puchisoft.multiplayerspacegame.net.Network.MovementState;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerJoinLeave;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerShoots;

public class WaoServer {
	Server server;
	private GameMap map;
	private Random random = new Random();

	public WaoServer() throws IOException {
//		Log.set(Log.LEVEL_DEBUG);
		
		server = new Server() {
			protected Connection newConnection() {
				// By providing our own connection implementation, we can store
				// per
				// connection state without a connection ID to state look up.
				return new WaoConnection();
			}
		};
		
		map = new GameMap(this);

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
					if(msg.version != Network.version){
						Log.error("wrong version");
						connection.close();
					}else{
						// Tell new person about map state (asteroids)
						connection.sendTCP(map.getStateData());
						// Tell old people about new person
						PlayerJoinLeave reply  = new PlayerJoinLeave(connection.getID(), connection.name, true, new Vector2(50,50), msg.color, 0);
						server.sendToAllExceptTCP(connection.getID(), reply);
						// Remember for our state too
						map.addPlayer(reply);
						
						// Tell new person about old people
						for(Connection con: server.getConnections()){
							WaoConnection conn = (WaoConnection)con;
							if(conn.getID() != connection.getID() && conn.name != null){ // Not self, Have logged in
								Player herePlayer = map.getPlayerById(conn.getID());
								// todo position in here is redundant
								PlayerJoinLeave hereMsg  = new PlayerJoinLeave(conn.getID(), herePlayer.getName(), true, herePlayer.getPosition(), herePlayer.getColor(), herePlayer.getScore());
								connection.sendTCP(hereMsg); // basic info
								connection.sendTCP(herePlayer.getMovementState()); // info about current movement
							}
						}
					}
					return;
				}
				else if(message instanceof MovementState) {
					MovementState msg = (MovementState)message;
					msg.playerId = connection.getID();
					// TODO Server updates its copy of player from what its told
					map.playerMoved(msg);
					server.sendToAllExceptTCP(connection.getID(), msg);
				}
				else if(message instanceof PlayerShoots) {
					PlayerShoots msg = (PlayerShoots)message;
					msg.playerID = connection.getID();
					// server validates that timeout wasn't too low, and only passes message on if valid
					if(map.onMsgPlayerShoots(msg)){ 
						server.sendToAllExceptTCP(connection.getID(), msg);
					}
				}
				else if(!(message instanceof Ping) && !(message instanceof KeepAlive)){
					Log.error("Client tried to send message only server sends/ or unknown");
				}
				
			}
			
			public void disconnected (Connection c) {
				WaoConnection connection = (WaoConnection)c;
				if (connection.name != null) {
					// Announce to everyone that someone has left.
					PlayerJoinLeave reply  = new PlayerJoinLeave(connection.getID(), connection.name, false, null, null, 0);
					server.sendToAllExceptTCP(connection.getID(), reply);
					map.removePlayer(reply);
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
	}
	
//	private WaoConnection getConnectionById(int id){
//		for(Connection con: server.getConnections()){
//			WaoConnection conn = (WaoConnection)con;
//			if (conn.getID() == id) return conn;
//		}
//		return null;
//	}

	public void shutdown() {
		server.close();
		server.stop();
	}

	public void sendMessage(Object message) {
		server.sendToAllTCP(message);
	}

}
