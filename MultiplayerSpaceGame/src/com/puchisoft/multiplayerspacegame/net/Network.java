package com.puchisoft.multiplayerspacegame.net;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.esotericsoftware.kryonet.FrameworkMessage.Ping;

public class Network {

	static public final int port = 6464;
	static public final int portUdp = 6466;
	static public final int version = 9;

	// This registers objects that are going to be sent over the network.
	static public void register(EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();
		kryo.register(Ping.class);
		kryo.register(Login.class);
//		kryo.register(LogMessage.class);
		kryo.register(Vector2.class);
		kryo.register(Color.class);
		kryo.register(PlayerJoinLeave.class);
		kryo.register(MovementChange.class);
		kryo.register(PlayerShoots.class);
		kryo.register(GameMapData.class);
		kryo.register(PlayerWasHit.class);
		kryo.register(AsteroidData.class);
		kryo.register(ArrayList.class);
		kryo.register(AsteroidWasHit.class);
	}

	static public class Login {
		public String name;
		public int version;
		public Color color;

		public Login() {
		}

		public Login(String name, int version, Color color) {
			this.name = name;
			this.version = version;
			this.color = color;
		}
	}

//	static public class LogMessage {
//		public String message;
//
//		public LogMessage() {
//		}
//
//		public LogMessage(String message) {
//			this.message = message;
//		}
//	}

	static public class PlayerJoinLeave {
		public int playerId;
		public String name;
		public boolean hasJoined;
		public Color color; // only for joined
		public int score;

		public PlayerJoinLeave() {
		}

		public PlayerJoinLeave(int playerId, String name, boolean hasJoined, Color color, int score) {
			this.playerId = playerId;
			this.name = name;
			this.hasJoined = hasJoined;
			this.color = color;
			this.score = score;
		}
	}

	static public class MovementChange {
		public int playerId;
		public int turning;
		public int accelerating;
		public float health;
		public Vector2 position;
		public Vector2 direction;
		public Vector2 velocity;

		public MovementChange() {
		}

		public MovementChange(int playerId, int turning, int accelerating, Vector2 position, Vector2 direction, Vector2 velocity, float health) {
			this.playerId = playerId;
			this.turning = turning;
			this.accelerating = accelerating;
			this.position = position;
			this.direction = direction;
			this.velocity = velocity;
			this.health = health;
		}
	}
	
	static public class PlayerWasHit {
		public int playerIdVictim;
		public int playerIdHitter;
		public float health;
		public PlayerWasHit() {}
		public PlayerWasHit(int playerIdVictim, int playerIdHitter, float health) {
			this.playerIdVictim = playerIdVictim;
			this.playerIdHitter = playerIdHitter;
			this.health = health;
		}
	}
	
	static public class AsteroidWasHit {
		public Vector2 position; // used as ID
		public AsteroidWasHit() {}
		public AsteroidWasHit(Vector2 position) {
			this.position = position;
		}
	}

	static public class PlayerShoots {
		public int playerID;
		public Vector2 position;
		public Vector2 direction;
		public Vector2 baseVelocity;
		public PlayerShoots() {}
		public PlayerShoots(int playerId, Vector2 position, Vector2 baseVelocity, Vector2 direction) {
			this.playerID = playerId;
			this.position = position;
			this.direction = direction;
			this.baseVelocity = baseVelocity;
		}
	}
	
	static public class GameMapData {
		public List<AsteroidData> asteroidDatas;
		public GameMapData() {}
		public GameMapData(List<AsteroidData> asteroidDatas) {
			this.asteroidDatas = asteroidDatas;
		}
	}

	
	static public class AsteroidData {
		public Vector2 position;
		public float rotation;
		public AsteroidData(){}
		public AsteroidData(Vector2 position, float rotation) {
			this.position = position;
			this.rotation = rotation;
		}
	}
	
}
