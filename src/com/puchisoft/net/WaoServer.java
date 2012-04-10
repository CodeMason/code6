package com.puchisoft.net;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import com.puchisoft.net.Network.LogMessage;
import com.puchisoft.net.Network.RegisterName;;

public class WaoServer {
	Server server;

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
			public void received(Connection c, Object object) {
				// We know all connections for this server are actually
				// SnatchConnections.
				WaoConnection connection = (WaoConnection) c;
				
				if (object instanceof RegisterName) {
					// Ignore the object if a client has already registered a name. This is
					// impossible with our client, but a hacker could send messages at any time.
					if (connection.name != null) return;
					// Ignore the object if the name is invalid.
					String name = ((RegisterName)object).name;
					if (name == null) return;
					name = name.trim();
					if (name.length() == 0) return;
					// Store the name on the connection.
					connection.name = name;
					
					server.sendToAllExceptTCP(connection.getID(), (RegisterName)object);
					
					// Send a "connected" message to everyone except the new client.
					LogMessage chatMessage = new LogMessage();
					chatMessage.message = name + " connected.";
					server.sendToAllExceptTCP(connection.getID(), chatMessage);
					return;
				}
				
			}
			
			public void disconnected (Connection c) {
				WaoConnection connection = (WaoConnection)c;
				if (connection.name != null) {
					// Announce to everyone that someone has left.
					LogMessage chatMessage = new LogMessage();
					chatMessage.message =  connection.name + " disconnected.";
					server.sendToAllExceptTCP(connection.getID(), chatMessage);
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
	}

	public void shutdown() {
		server.close();
		server.stop();
	}

}
