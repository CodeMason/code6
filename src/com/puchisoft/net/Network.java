package com.puchisoft.net;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.FrameworkMessage.Ping;

public class Network {

	static public final int port = 6464;
	static public final int version = 1;

	// This registers objects that are going to be sent over the network.
	static public void register (EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();
		kryo.register(Ping.class);
		kryo.register(Login.class);
		kryo.register(LogMessage.class);
		kryo.register(PlayerJoinLeave.class);
		kryo.register(MovementChange.class);
		kryo.register(Vector2.class);
	}
	
	static public class Login {
		public String name;
		public int version;
		public Login() {}
		public Login(String name, int version) {
			this.name = name;
			this.version = version;
		}
	}

	static public class LogMessage {
		public String message;
		
		public LogMessage(){}
		public LogMessage(String message) {
			this.message = message;
		}
	}
	
	static public class PlayerJoinLeave {
		public int playerId;
		public String name;
		public boolean hasJoined; 
		
		public PlayerJoinLeave(){}
		public PlayerJoinLeave(int playerId, String name, boolean hasJoined) {
			this.playerId = playerId;
			this.name = name;
			this.hasJoined = hasJoined;
		}
	}
	
	static public class MovementChange {
		public int playerId;
		public boolean isMoving;
		public Vector2 position;
		public Vector2 direction;
		
		public MovementChange(){}
		public MovementChange(int playerId, boolean isMoving, Vector2 position, Vector2 direction) {
			this.playerId = playerId;
			this.direction = direction;
			this.isMoving = isMoving;
			this.position = position;
		}
		
	}
}
