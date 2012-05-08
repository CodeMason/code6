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
import com.puchisoft.multiplayerspacegame.net.Network.GameConfigData;
import com.puchisoft.multiplayerspacegame.net.Network.MovementChange;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerJoinLeave;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerShoots;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerWasHit;
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

	private Vector2 maxPosition;

	private WaoClient client;
	private HUD hud;
	private TextureRegion textureAstroid;
	
	boolean isClient;

	public GameMap(boolean isClient) {
		this.isClient = isClient;
		if(isClient) this.hud = new HUD();
		Gdx.files.internal("data/background.png");
		textureBg = new Texture(Gdx.files.internal("data/background.png"));

		texturePlayer = new TextureRegion(new Texture(Gdx.files.internal("data/player.png")), 0, 0, 42, 32);
		textureBullet = new TextureRegion(new Texture(Gdx.files.internal("data/bullet.png")), 0, 0, 32, 6);
		textureAstroid = new TextureRegion(new Texture(Gdx.files.internal("data/asteroid.png")), 0, 0, 64, 64);
		
		fontNameTag = new BitmapFont();
		fontNameTag.setColor(Color.YELLOW);
//		fontNameTag.setScale(0.9f);

		maxPosition = new Vector2(textureBg.getWidth() * tilesCount, textureBg.getHeight() * tilesCount);
		
		this.cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		spriteBatch = new SpriteBatch(); //

	}
	
	private void handleInput(float delta){
		
		// Zooming
		if (Gdx.input.isKeyPressed(Keys.Q)){
			this.cam.zoom = Math.max(this.cam.zoom - 1.0f * delta , 0.3f);
		}else if (Gdx.input.isKeyPressed(Keys.E)){
			this.cam.zoom = Math.min(this.cam.zoom + 1.0f * delta, 10.0f);
		}
		else if (Gdx.input.isTouched(1)){
			this.cam.zoom = Math.min(Math.max(this.cam.zoom + (Gdx.input.getDeltaX(1) * 0.5f * delta), 0.3f), 10.0f);
		}
	}

	public synchronized void update(float delta){
		
		// Client ready and player is spawned 
		if (client != null && playerLocal != null) {
			handleInput(delta);
			
			if(!playerLocal.isDead()){
				this.cam.position.set(playerLocal.getDesiredCameraPosition(this.cam.position, delta));
			}else{
				Player killer = players.get(playerLocal.getLastHitter());
				if(killer != null){
					this.cam.position.set(killer.getDesiredCameraPosition(this.cam.position, delta));
				}
			}
			
			cam.update();
			
			// Handle local input and sent over network if changed
			if (playerLocal.handleInput(delta)) {
				Log.info("changed input");
				client.sendMessage(playerLocal.getMovementState());
			}
		}

		// Update Players
		for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
			Player playerCur = playerEntry.getValue();
			playerCur.update(delta);
			// Collision with asteroids
			for(Asteroid asteroid : asteroids){
				if(playerCur.getBoundingRectangle().overlaps(asteroid.getBoundingRectangle())){
					playerCur.preventOverlap(asteroid.getBoundingRectangle(),delta);
//					playerCur.hit(5, -1);
				}
			}
			// Collision with Players
//			for (Map.Entry<Integer, Player> otherPlayerEntry : players.entrySet()) {
//				Player playerCurOther = otherPlayerEntry.getValue();
//				if(playerCurOther.getID() != playerCur.getID() && playerCurOther.getBoundingRectangle().overlaps(playerCur.getBoundingRectangle())){
//					playerCur.preventOverlap(playerCurOther.getBoundingRectangle(),delta);
//					playerCurOther.preventOverlap(playerCur.getBoundingRectangle(),delta);
//				}
//			}
		}

		// Update Bullets
		for (int i = 0; i < bullets.size(); i++) {
			Bullet bulletCur = bullets.get(i);
			bullets.get(i).update(delta);
			// Collision with Players
			for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
				Player playerCur = playerEntry.getValue();
				if(!playerCur.isDead() && playerCur.getID() != bulletCur.getPlayerID() && playerCur.getBoundingRectangle().overlaps(bulletCur.getBoundingRectangle())){
					bulletCur.destroy();
					playerCur.hit(40, bulletCur.getPlayerID());
					// I was hit
					if(playerCur == playerLocal){
						Gdx.input.vibrate(300);
						players.get(bulletCur.getPlayerID()).addScore(1);
						setStatus("You were hit by "+players.get(bulletCur.getPlayerID()).getName()+"!");
						client.sendMessage(new PlayerWasHit(playerLocal.getID(),bulletCur.getPlayerID(),playerLocal.getHealth()));
					}
				}
			}
			// Collision with Asteroids
			for(Asteroid asteroid : asteroids){
				if(bulletCur.getBoundingRectangle().overlaps(asteroid.getBoundingRectangle())){
					bulletCur.destroy();
				}
			}
			
			// Remove impacted bullets
			if (bullets.get(i).destroyed)
				bullets.remove(i);
		}
	}
	
	public synchronized void render(){
		
		spriteBatch.setProjectionMatrix(cam.combined);
		spriteBatch.begin();
		spriteBatch.setColor(Color.WHITE);
		
		// Background
		for (int i = 0; i < tilesCount; i++) {
			for (int j = 0; j < tilesCount; j++) {
				spriteBatch.draw(textureBg, textureBg.getWidth() * i, textureBg.getHeight() * j, 0, 0, textureBg.getWidth(),
						textureBg.getHeight());
			}
		}
		
		// Render Asteroids
		for (int i = 0; i < asteroids.size(); i++) {
			asteroids.get(i).render(spriteBatch);
		}
		
		// Render Players
		for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
			Player curPlayer = playerEntry.getValue();
			curPlayer.render(spriteBatch);
			if(curPlayer != playerLocal) curPlayer.renderNameTag(spriteBatch, fontNameTag);
		}
		
		// Render Bullets
		for (int i = 0; i < bullets.size(); i++) {
			bullets.get(i).render(spriteBatch);
		}
		
		spriteBatch.end();
		
		if(hud != null) hud.render();
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
			playerLocal = new Player(texturePlayer, new Vector2(50, 50), maxPosition, this, color, true);
			this.playerLocal.setId(client.id);
			this.playerLocal.setName(name);
			players.put(client.id, playerLocal);
			hud.setPlayerLocal(playerLocal);
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

	public synchronized void addPlayer(PlayerJoinLeave msg) {
		Log.info("add player");
		Player newPlayer = new Player(texturePlayer, new Vector2(50, 50), maxPosition, this, msg.color, false);
		newPlayer.setId(msg.playerId);
		newPlayer.setName(msg.name);
		newPlayer.addScore(msg.score);
		players.put(msg.playerId, newPlayer);

		// tell people where I am again
		// TODO server should remember this and tell others based on emulating players movements locally
		client.sendMessage(playerLocal.getMovementState());
	}

	public synchronized void removePlayer(PlayerJoinLeave msg) { // synchronized
		Log.info("remove player");
		players.remove(msg.playerId);
	}

	public synchronized void playerMoved(MovementChange msg) {
		Player player = players.get(msg.playerId);
		player.setMovementState(msg);

	}

	public void setStatus(String status) {
		if(hud != null) hud.setStatus(status);
	}

	public synchronized void addBullet(PlayerShoots playerShoots) {
		if (playerShoots.playerID == playerLocal.getID()) {
			// tell others I shot
			client.sendMessage(playerShoots);
		}
		bullets.add(new Bullet(textureBullet, playerShoots.playerID, playerShoots.position, playerShoots.baseVelocity, playerShoots.direction, maxPosition));
	}
	
	private void addAsteroid(Vector2 postion, float rotation){
		asteroids.add(new Asteroid(textureAstroid, postion, rotation));
	}
	
	public synchronized void generateMap(GameConfigData gameConfigData){
		
		random.setSeed(gameConfigData.mapGeneratorSeed);
		// Generate asteroids
		int randomX = 0;
		int randomY = 0;
		int loopExit = 0;
		while(asteroids.size() < gameConfigData.asteroidQuantity && loopExit < 1000){
			loopExit++;
			randomX = random.nextInt((int) maxPosition.x - textureAstroid.getRegionWidth()-100) + 100;
			randomY = random.nextInt((int) maxPosition.y - textureAstroid.getRegionHeight()-100) + 100;
			Rectangle box = new Rectangle(randomX , randomY, textureAstroid.getRegionWidth(), textureAstroid.getRegionHeight());
			boolean canMakeAsteroid = true;
			for (int j = 0; j < asteroids.size(); j++) {
				if(asteroids.get(j).getBoundingRectangle().overlaps(box)){
					canMakeAsteroid = false;
					break;
				}
			}
			if(canMakeAsteroid){
				addAsteroid(new Vector2(randomX,randomY),random.nextInt(360));
			}
			
		}
	}

	public void resize(int width, int height) {
		cam.setToOrtho(false, width, height);
		hud.resize(width, height);
	}

	public void onMsgPlayerWasHit(PlayerWasHit msg) {
		Player hitter = players.get(msg.playerIdHitter);
		Player victim = players.get(msg.playerIdVictim);
		// give hitter points
		if(hitter != null){
			hitter.addScore(1);
			if(hitter == playerLocal){
				if(victim != null) setStatus("You hit "+victim.getName()+"!");
			}else if(victim != null){
				if(victim != null) setStatus(hitter.getName()+" "+msg.playerIdHitter+" hit "+victim.getName()+".");
			}
		}
		// Update victim's health
		if(victim != null){
			victim.setHealth(msg.health);
		}
	}
	
	public void sendMessage(Object msg){
		client.sendMessage(msg);
	}
}
