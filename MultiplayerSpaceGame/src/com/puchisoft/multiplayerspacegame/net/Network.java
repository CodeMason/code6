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
	static public final int version = 13;

	// This registers objects that are going to be sent over the network.
	static public void register(EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();
		kryo.register(Ping.class);
		kryo.register(Login.class);
//		kryo.register(LogMessage.class);
		kryo.register(Vector2.class);
		kryo.register(Color.class);
		kryo.register(PlayerJoinLeave.class);
		kryo.register(MovementState.class);
		kryo.register(PlayerSpawns.class);
		kryo.register(PlayerShoots.class);
		kryo.register(GameMapData.class);
		kryo.register(PlayerWasHit.class);
		kryo.register(AsteroidData.class);
		kryo.register(ArrayList.class);
		kryo.register(AsteroidWasHit.class);
		kryo.register(RoundEnd.class);
		kryo.register(RoundStart.class);
	}

	static public class Login {
		public String name;
		public int version;
		public Color color;

		public Login() {}
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
		public Vector2 position;
		public float health;
		

		public PlayerJoinLeave() {}
		public PlayerJoinLeave(int playerId, String name, boolean hasJoined, Vector2 position, Color color, int score, float health) {
			this.playerId = playerId;
			this.name = name;
			this.hasJoined = hasJoined;
			this.position = position; // remove, we need to send a movementmsg on join anyaway
			this.color = color;
			this.score = score;
			this.health = health;
		}
	}
	
	/*
	 * Movement
	 */
	static public class MovementState {
		public int playerId;
		public int turning;
		public int accelerating;
		public Vector2 position;
		public Vector2 direction;
		public Vector2 velocity;

		public MovementState() {}
		public MovementState(int playerId, int turning, int accelerating, Vector2 position, Vector2 direction, Vector2 velocity) {
			this.playerId = playerId;
			this.turning = turning;
			this.accelerating = accelerating;
			this.position = position;
			this.direction = direction;
			this.velocity = velocity;
		}
	}
	
	static public class PlayerSpawns{
		public int playerId;
		public MovementState movementState;

		public PlayerSpawns() {}
		public PlayerSpawns(int playerId, MovementState movementState) {
			this.playerId = playerId;
			this.movementState = movementState;
		}
	}
	/*
	 * Shooting and Hitting
	 */
	static public class PlayerWasHit {
		public int playerIdVictim;
		public int playerIdHitter;
		public float damage;
		
		public PlayerWasHit() {}
		public PlayerWasHit(int playerIdVictim, int playerIdHitter, float damage) {
			this.playerIdVictim = playerIdVictim;
			this.playerIdHitter = playerIdHitter;
			this.damage = damage;
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
	
	static public class AsteroidWasHit {
		public Vector2 position; // used as ID
		
		public AsteroidWasHit() {}
		public AsteroidWasHit(Vector2 position) {
			this.position = position;
		}
	}
	
	/*
	 * Map Data 
	 */
	// Sent to someone when they join, also sent to existing people when a new round begins to reset map / fill with this data
	static public class GameMapData {
		public boolean roundOver;
		public List<AsteroidData> asteroidDatas;
		
		public GameMapData() {}
		
		public GameMapData(List<AsteroidData> asteroidDatas, boolean roundOver) {
			this.asteroidDatas = asteroidDatas;
			this.roundOver = roundOver;
		}
	}
	
	// Used in GameMapData, or stand-alone to mean adding a new asteroid
	static public class AsteroidData {
		public Vector2 position;
		public float rotation;
		public AsteroidData(){}
		public AsteroidData(Vector2 position, float rotation) {
			this.position = position;
			this.rotation = rotation;
		}
	}
	
	/*
	 * Rounds
	 */
	static public class RoundEnd {
		public int winnerID;

		public RoundEnd(){}
		public RoundEnd(int winnerID) {
			this.winnerID = winnerID;
		}
	}

	static public class RoundStart {
		
		public RoundStart(){}
	}
}
