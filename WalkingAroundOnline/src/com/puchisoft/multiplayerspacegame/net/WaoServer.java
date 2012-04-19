package com.puchisoft.multiplayerspacegame.net;

import java.io.IOException;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.multiplayerspacegame.net.Network.Login;
import com.puchisoft.multiplayerspacegame.net.Network.MovementChange;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerJoinLeave;

public class WaoServer {
	Server server;
//	Map<Integer,WaoConnection> connections = new HashMap<Integer,WaoConnection>();

	public WaoServer() throws IOException {
		//Log.set(Log.LEVEL_DEBUG);
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
				// We know all connections for this server are actually
				// SnatchConnections.
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
					}
					
					server.sendToAllExceptTCP(connection.getID(), msg);
					
					// Tell old people about new person
					PlayerJoinLeave reply  = new PlayerJoinLeave(connection.getID(), connection.name, true);
					server.sendToAllExceptTCP(connection.getID(), reply);
					
					// Tell new person about old people
					for(Connection con: server.getConnections()){
						WaoConnection conn = (WaoConnection)con;
						if(conn.getID() != connection.getID() && conn.name != null){ // Not self, Have logged in
							PlayerJoinLeave hereMsg  = new PlayerJoinLeave(conn.getID(), conn.name, true);
							connection.sendTCP(hereMsg);
						}
					}
					return;
				}
				else if(message instanceof MovementChange) {
					MovementChange msg = (MovementChange)message;
					msg.playerId = connection.getID();
					connection.position = msg.position;
					server.sendToAllExceptTCP(connection.getID(), msg);
				}
				
			}
			
			public void disconnected (Connection c) {
				WaoConnection connection = (WaoConnection)c;
				if (connection.name != null) {
					// Announce to everyone that someone has left.
					PlayerJoinLeave reply  = new PlayerJoinLeave(connection.getID(), connection.name, false);
					server.sendToAllExceptTCP(connection.getID(), reply);
				}
			}
		});
		
		server.bind(Network.port);
		server.start();
	}
	
	public void stop() {
		server.stop();
	}
	
	// Connection specific attributes
	static class WaoConnection extends Connection {
		public String name;
		public Vector2 position;
	}

	public void shutdown() {
		server.close();
		server.stop();
	}

}