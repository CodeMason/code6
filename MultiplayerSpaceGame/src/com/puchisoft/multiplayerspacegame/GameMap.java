package com.puchisoft.multiplayerspacegame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.multiplayerspacegame.net.Network.AstroidLocations;
import com.puchisoft.multiplayerspacegame.net.Network.MovementChange;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerJoinLeave;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerShoots;
import com.puchisoft.multiplayerspacegame.net.WaoClient;

public class GameMap {
	public OrthographicCamera cam;
	private SpriteBatch spriteBatch;

	private Random random = new Random();

	private Map<Integer, Player> players = new HashMap<Integer, Player>();
	private List<Bullet> bullets = new ArrayList<Bullet>();
	private List<Asteroid> asteroids = new ArrayList<Asteroid>();

	private Player playerLocal;

	public BitmapFont fontNameTag;

	private TextureRegion texturePlayer;
	private TextureRegion textureBullet;
	private Texture textureBg;
	private int tilesCount = 3;

	private Texture texturemoon;
	private Texture textureChasemoon;
	private Vector2 sizemoon;

	private Vector2 maxPosition;

	private WaoClient client;
	private HUD hud;
	private TextureRegion textureAstroid;

	boolean isClient;

	private Moon moonChase;
	private Vector2 currentPlayerPosition;

	public GameMap(boolean isClient) {
		this.isClient = isClient;
		if (isClient)
			this.hud = new HUD();

		Gdx.files.internal("data/background.png");
		textureBg = new Texture(Gdx.files.internal("data/background.png"));

		texturePlayer = new TextureRegion(new Texture(Gdx.files.internal("data/player.png")), 0, 0, 42, 32);
		textureBullet = new TextureRegion(new Texture(Gdx.files.internal("data/bullet.png")), 0, 0, 32, 6);
		texturemoon = new Texture(Gdx.files.internal("data/moon.png"));
		textureChasemoon = new Texture(Gdx.files.internal("data/moonchase.png"));

		textureAstroid = new TextureRegion(new Texture(Gdx.files.internal("data/asteroid.png")), 0, 0, 64, 64);

		fontNameTag = new BitmapFont();
		fontNameTag.setColor(Color.YELLOW);
		fontNameTag.setScale(0.9f);

		maxPosition = new Vector2(textureBg.getWidth() * tilesCount,
				textureBg.getHeight() * tilesCount);

		moonChase = new Moon(textureChasemoon, new Vector2(50, 50), maxPosition);

		this.cam = new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
		sizemoon = new Vector2(texturemoon.getWidth(), texturemoon.getHeight());
		
		moonChase.setPosition(maxPosition.cpy().mul(0.5f));

		spriteBatch = new SpriteBatch(); //

	}

	private void handleInput(float delta) {
		if (client == null || playerLocal == null) {
			return;
		}

		// Zooming
		if (Gdx.input.isKeyPressed(Keys.Q)) {
			this.cam.zoom = Math.max(this.cam.zoom - 1.0f * delta, 0.3f);
		} else if (Gdx.input.isKeyPressed(Keys.E)) {
			this.cam.zoom = Math.min(this.cam.zoom + 1.0f * delta, 10.0f);
		} else if (Gdx.input.isTouched(1)) {
			this.cam.zoom = Math.min(Math.max(this.cam.zoom	+ (Gdx.input.getDeltaX(1) * 0.5f * delta), 0.3f), 10.0f);
		}
	}

	public void update(float delta) {
		
		if (client == null || playerLocal == null) {
			return;
		}

		// Client ready and player is spawned
		if (client != null && playerLocal != null) {
			handleInput(delta);

			this.cam.position.set(playerLocal.getDesiredCameraPosition(
					this.cam.position, delta));
			cam.update();

			// Handle local input and sent over network if changed
			if (playerLocal.handleInput(delta)) {
				Log.info("changed input");
				client.sendMessage(playerLocal.moon.getMovementState());
			}
		}

		Player closestPlayer = playerLocal;
		float moonChaseDist = 999999999;

		Vector2 gravitySum;
		float gravityForce;
		
		//Gravity Constants
		float gravityShip = (0.0000000000667384f * 7360000000000f * 3040f);
		float gravityBullet = (0.0000000000667384f * 7360000000000f * 340f);
		float gravityAsteroid = (0.0000000000667384f * 7360000000f * 340f);

		for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {

			// Chase Moon
			float currentdist = playerEntry.getValue().getPosition().dst2(moonChase.getPosition());
			if (currentdist < moonChaseDist) {
				moonChaseDist = currentdist;
				closestPlayer = playerEntry.getValue();
			}

			// Gravity

			// gravityForce = 0;
			// if (playerLocal != playerEntry.getValue()) {
			// if
			// (!playerLocal.moon.position.equals(playerEntry.getValue().moon.position)){
			// gravityForce =(gravityShip /
			// playerLocal.moon.position.dst2(playerEntry.getValue().position));
			// if (gravityForce > 10){
			// gravityForce = 10;
			// }
			// Vector2 gravityVector =
			// playerEntry.getValue().position.cpy().sub(playerLocal.moon.position).nor().mul(gravityForce);
			// playerLocal.moon.velocity.add(gravityVector.mul(delta));
			// Log.info(String.valueOf(gravityForce));
			// }
			// }
			
			//Gravity on Players from Moons
			for (Map.Entry<Integer, Player> playerEntryMoon : players
					.entrySet()) {
				gravityForce = 0;
				if (playerEntryMoon.getValue() != playerEntry.getValue()) {
					if (!playerEntryMoon.getValue().moon.getPosition().equals(playerEntry.getValue().moon.getPosition())) {
						gravityForce = (gravityShip / playerEntryMoon.getValue().moon.getPosition().dst2(moonChase.getPosition()));
						if (gravityForce > 10) {
							gravityForce = 10;
						}
						Vector2 gravityVector = moonChase.getPosition().cpy().sub(playerEntryMoon.getValue().moon.getPosition()).nor().mul(gravityForce);
						playerEntryMoon.getValue().moon.velocity.add(gravityVector.mul(delta));
						Log.info(String.valueOf(gravityForce));
					}
				}
				//Gravity on Players from Chase Moon (Gravity Well)
				gravityForce = (gravityAsteroid / playerEntryMoon.getValue().moon.getPosition().dst2(playerEntry.getValue().getPosition()));
				if (gravityForce > 15) {
					gravityForce = 15;
				}
				Vector2 gravityVector = moonChase.getPosition().cpy().sub(playerEntryMoon.getValue().moon.getPosition()).nor().mul(gravityForce);
				playerEntryMoon.getValue().moon.velocity.add(gravityVector.mul(delta));
				Log.info(String.valueOf(gravityForce));
			}
			//Gravity on Bullets from Moons
//			for (int i = 0; i < bullets.size(); i++) {
//				Bullet bulletCur = bullets.get(i);
//				gravityForce = 0;
//				if (!bulletCur.getPosition().equals(
//						playerEntry.getValue().moon.getPosition())) {
//					gravityForce = (gravityBullet / bulletCur.getPosition()
//							.dst2(playerEntry.getValue().moon.getPosition()));
//					if (gravityForce > 10) {
//						gravityForce = 10;
//					}
//					Vector2 gravityVector = playerEntry.getValue().moon.getPosition()
//							.cpy().sub(bulletCur.getPosition()).nor()
//							.mul(gravityForce);
//					bulletCur.setVelocity(bulletCur.getVelocity().add(
//							gravityVector.mul(delta)));
//					Log.info(String.valueOf(gravityForce));
//				}
//
//			}
		}

		for (int i = 0; i < bullets.size(); i++) {
			Bullet bulletCurI = bullets.get(i);
			gravityForce = 0;
			//Gravity on Bullets from Bullets - Overkilled
//			for (int j = 0; j < bullets.size(); j++) {
//				Bullet bulletCurJ = bullets.get(j);
//				gravityForce = 0;
//				if (!bulletCurI.getPosition().equals(bulletCurJ.getPosition())) {
//					gravityForce = (gravityShip / bulletCurI.getPosition()
//							.dst2(bulletCurJ.getPosition()));
//					if (gravityForce > 15) {
//						gravityForce = 15;
//					}
//					Vector2 gravityVector = bulletCurJ.getPosition().cpy().sub(bulletCurI.getPosition()).nor().mul(gravityForce);
//					bulletCurI.setVelocity(bulletCurI.getVelocity().add(gravityVector.mul(delta)));
//					Log.info(String.valueOf(gravityForce));
//				}
//
//			}
			//Gravity on Bullets from Asteroids
			for (int j = 0; j < asteroids.size(); j++) {
				Asteroid asteroidCurJ = asteroids.get(j);
				gravityForce = 0;
				if (!bulletCurI.getPosition().equals(asteroidCurJ.getPosition())) {
					gravityForce = (gravityAsteroid / bulletCurI.getPosition()
							.dst2(asteroidCurJ.getPosition()));
					if (gravityForce > 15) {
						gravityForce = 15;
					}
					Vector2 gravityVector = asteroidCurJ.getPosition().cpy().sub(bulletCurI.getPosition()).nor().mul(gravityForce);
					bulletCurI.setVelocity(bulletCurI.getVelocity().add(gravityVector.mul(delta)));
					Log.info(String.valueOf(gravityForce));
				}

			}
		}
		//Moves Chase Moon towards closest Player
		//moonChase.getPosition().lerp(closestPlayer.getPosition(), 0.01f);

		// Update Players
		for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
			Player playerCur = playerEntry.getValue();
			playerCur.update(delta);
			// Collision with asteroids
			for (Asteroid asteroid : asteroids) {
				if (playerCur.getBoundingRectangle().overlaps(asteroid.getBoundingRectangle())) {
					playerCur.preventOverlap(asteroid.getBoundingRectangle(),delta);
				}
				if (playerCur.moon.getBoundingRectangle().overlaps(asteroid.getBoundingRectangle())) {
					playerCur.moon.preventOverlap(asteroid.getBoundingRectangle(),delta);
				}
			}
		}

		// Update Bullets
		for (int i = 0; i < bullets.size(); i++) {
			Bullet bulletCur = bullets.get(i);
			bullets.get(i).update(delta);
			// Collision with Players
			for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
				Player playerCur = playerEntry.getValue();
				if (playerCur.getID() != bulletCur.getPlayerID()
						&& playerCur.getBoundingRectangle().overlaps(
								bulletCur.getBoundingRectangle())) {
					bulletCur.destroy();
					playerCur.hit();
					if (playerCur == playerLocal) {
						Gdx.input.vibrate(300);
						client.sendMessage(playerLocal.getMovementState());
					}
				}
				if (bulletCur.getBoundingRectangle().overlaps(playerCur.moon.getBoundingRectangle())) {
					bulletCur.preventOverlap(playerCur.moon.getBoundingRectangle(),delta);
				}
			}
			
			for (Asteroid asteroid : asteroids) {
				if (bulletCur.getBoundingRectangle().overlaps(asteroid.getBoundingRectangle())) {
					bulletCur.preventOverlap(asteroid.getBoundingRectangle(),delta);
				}
			}

			// Remove impacted bullets
			if (bullets.get(i).destroyed)
				bullets.remove(i);
		}

	}

	public void render() {
		if (client == null || playerLocal == null) {
			return;
		}

		spriteBatch.setProjectionMatrix(cam.combined);
		spriteBatch.begin();
		spriteBatch.setColor(Color.WHITE);

		// Background
		for (int i = 0; i < tilesCount; i++) {
			for (int j = 0; j < tilesCount; j++) {
				spriteBatch.draw(textureBg, textureBg.getWidth() * i,
						textureBg.getHeight() * j, 0, 0, textureBg.getWidth(),
						textureBg.getHeight());
			}
		}

		// Render Asteroids
		for (int i = 0; i < asteroids.size(); i++) {
			asteroids.get(i).render(spriteBatch);
		}

		// Render Players
		for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
			playerEntry.getValue().render(spriteBatch);
			if (playerEntry.getValue() != playerLocal)
				playerEntry.getValue().renderNameTag(spriteBatch, fontNameTag);
		}
		// Render Bullets
		for (int i = 0; i < bullets.size(); i++) {
			bullets.get(i).render(spriteBatch);
		}

		moonChase.render(spriteBatch);

		spriteBatch.end();

		if (hud != null)
			hud.render();
	}

	public void dispose() {
		texturePlayer.getTexture().dispose();
		textureBg.dispose();
		spriteBatch.dispose();
		hud.dispose();
	}

	public void onConnect(WaoClient client, String name, Color color) {

		if (this.client == null) {
			this.client = client;
			Moon moonLocal = new Moon(texturemoon, new Vector2(50, 50),
					maxPosition);

			playerLocal = new Player(texturePlayer, new Vector2(50, 50),
					maxPosition, this, color, moonLocal);
			this.playerLocal.setId(client.id);
			this.playerLocal.setName(name);
			players.put(client.id, playerLocal);
			setStatus("Connected to " + client.remoteIP);
		} else {
			Log.error("setNetworkClient called twice");
		}
	}

	public void onDisconnect() {
		this.client = null;
		this.players.clear();
		setStatus("Disconnected");
	}

	public void addPlayer(PlayerJoinLeave msg) {
		Log.info("add player");
		Player newPlayer = new Player(texturePlayer, new Vector2(50, 50),
				maxPosition, this, msg.color, new Moon(texturemoon,
						new Vector2(50, 50), maxPosition));
		newPlayer.setId(msg.playerId);
		newPlayer.setName(msg.name);
		players.put(msg.playerId, newPlayer);

		// tell people where I am again
		// TODO server should remember this and tell others based on emulating
		// players movements locally
		client.sendMessage(playerLocal.getMovementState());
		//client.sendMessage(playerLocal.moon.getMovementState());
	}

	public void removePlayer(PlayerJoinLeave msg) {
		Log.info("remove player");
		players.remove(msg.playerId);
	}

	public void playerMoved(MovementChange msg) {
		Player player = players.get(msg.playerId);
		player.setMovementState(msg);
		//player.moon.setMovementState(msg);

	}

	public void setStatus(String status) {
		if (hud != null)
			hud.setStatus(status);
	}

	public void addBullet(PlayerShoots playerShoots) {
		if (playerShoots.playerID == playerLocal.getID()) {
			// tell others I shot
			client.sendMessage(playerShoots);
		}
		bullets.add(new Bullet(textureBullet, playerShoots.playerID,
				playerShoots.position, playerShoots.baseVelocity,
				playerShoots.direction, maxPosition));
	}

	private void addAsteroid(Vector2 postion) {
		asteroids.add(new Asteroid(textureAstroid, postion));
	}

	public void addAsteroidsRandom(int amount) {
		//Randomly generates Asteroid locations
		int randomX = 0;
		int randomY = 0;
		int loopExit = 0;
		Log.error("initial" + loopExit);
		
		//Prevents Asteroids from overlapping
		while (asteroids.size() < amount && loopExit < 1000) {
			loopExit++;
			Log.info("current" + loopExit);
			randomX = random.nextInt((int) maxPosition.x
					- textureAstroid.getRegionWidth() - 100) + 100;
			randomY = random.nextInt((int) maxPosition.y
					- textureAstroid.getRegionHeight() - 100) + 100;
			Rectangle box = new Rectangle(randomX, randomY,
					textureAstroid.getRegionWidth(),
					textureAstroid.getRegionHeight());
			boolean canMakeAsteroid = true;
			for (int j = 0; j < asteroids.size(); j++) {
				if (asteroids.get(j).getBoundingRectangle().overlaps(box)) {
					canMakeAsteroid = false;
				}
			}
			if (canMakeAsteroid == true) {
				addAsteroid(new Vector2(randomX, randomY));
			}

		}
	}

	public AstroidLocations getAstroidLocations() {
		Vector2[] positions = new Vector2[asteroids.size()];
		for (int i = 0; i < asteroids.size(); i++) {
			positions[i] = asteroids.get(i).getPosition();
		}

		return new AstroidLocations(positions);
	}

	public void addAstroidLocations(AstroidLocations astroidLocations) {
		for (Vector2 positon : astroidLocations.positions) {
			addAsteroid(positon);
		}
	}
}
