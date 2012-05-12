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
import com.puchisoft.multiplayerspacegame.net.Network.AsteroidData;
import com.puchisoft.multiplayerspacegame.net.Network.AsteroidWasHit;
import com.puchisoft.multiplayerspacegame.net.Network.GameMapData;
import com.puchisoft.multiplayerspacegame.net.Network.MovementChange;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerJoinLeave;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerShoots;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerWasHit;
import com.puchisoft.multiplayerspacegame.net.WaoClient;
import com.puchisoft.multiplayerspacegame.net.WaoServer;

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

	private WaoClient client; // only if I'm the client
	private WaoServer server; // only if I'm internal to the server
	private HUD hud;
	private TextureRegion textureAsteroid;
	private TextureRegion textureAsteroidGold;
	
	private GameSounds gameSounds;
	
	boolean isClient;

	/*
	 * For client
	 */
	public GameMap(WaoClient client) {
		this.client = client;
		this.isClient = true;
		
		initCommon();
		
		this.hud = new HUD();
		this.gameSounds = new GameSounds();
		this.fontNameTag = new BitmapFont();
		this.fontNameTag.setColor(Color.YELLOW);
		
		this.cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		spriteBatch = new SpriteBatch(); //
	}
	
	/*
	 * For server
	 */
	public GameMap(WaoServer server) {
		this.server = server;
		this.isClient = false;
		
		initCommon();
		
	}

	private void initCommon(){
		textureBg = new Texture(Gdx.files.internal("data/background.png"));

		texturePlayer = new TextureRegion(new Texture(Gdx.files.internal("data/player.png")), 0, 0, 42, 32);
		textureBullet = new TextureRegion(new Texture(Gdx.files.internal("data/bullet.png")), 0, 0, 32, 6);
		textureAsteroid = new TextureRegion(new Texture(Gdx.files.internal("data/asteroid.png")), 0, 0, 64, 64);
		textureAsteroidGold = new TextureRegion(new Texture(Gdx.files.internal("data/asteroid_gold.png")), 0, 0, 64, 64);
		
		maxPosition = new Vector2(textureBg.getWidth() * tilesCount, textureBg.getHeight() * tilesCount);
	}
	
	
	private void handleInput(float delta){
		
		// Zooming
		if (Gdx.input.isKeyPressed(Keys.Q)){
			this.cam.zoom = Math.max(this.cam.zoom - 1.0f * delta , 0.3f);
		}else if (Gdx.input.isKeyPressed(Keys.E)){
			this.cam.zoom = Math.min(this.cam.zoom + 1.0f * delta, 10.0f);
		}
//		else if (Gdx.input.isTouched(1)){
//			this.cam.zoom = Math.min(Math.max(this.cam.zoom + (Gdx.input.getDeltaX(1) * 0.5f * delta), 0.3f), 10.0f);
//		}
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
				logInfo("changed input");
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
					logInfo("Player touched asteroid");
				}
			}
			
			// Spawn dead players (sends a message to clients)
			if(!isClient) playerCur.spawnIfAppropriate();
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
					if(!isClient){
						PlayerWasHit msg = new PlayerWasHit(playerCur.getID(),bulletCur.getPlayerID(), 45);
						this.onMsgPlayerWasHit(msg);
						server.sendMessage(msg);
					}
				}
			}
			// Collision with Asteroids
			for(Asteroid asteroid : asteroids){
				if(bulletCur.getBoundingRectangle().overlaps(asteroid.getBoundingRectangle())){
					bulletCur.destroy();
					// Only server makes call whether asteroids are killed
					if(!isClient){
						server.sendMessage(new AsteroidWasHit(asteroid.getPosition()));
						asteroid.destroy();
					}
				}
			}
			
			// Remove impacted bullets
			if (bullets.get(i).destroyed()){
				bullets.remove(i);
			}
		}

		// Remove asteroids
		for (int i = 0; i < asteroids.size(); i++) {
			if (asteroids.get(i).destroyed){
				asteroids.remove(i);
			}
		}
	}
	
	public synchronized void logInfo(String string) {
		Log.info((isClient ? "[Client] " : "[Server] ")+string);
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
		gameSounds.dispose();
	}

	public void onConnect(String name, Color color) {

		if (this.playerLocal == null) {
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
		Log.debug("add player");
		Player newPlayer = new Player(texturePlayer, new Vector2(50, 50), maxPosition, this, msg.color, false);
		newPlayer.setId(msg.playerId);
		newPlayer.setName(msg.name);
		newPlayer.addScore(msg.score);
		players.put(msg.playerId, newPlayer);

	}

	public synchronized void removePlayer(PlayerJoinLeave msg) { // synchronized
		logInfo("remove player");
		players.remove(msg.playerId);
	}

	public synchronized void playerMoved(MovementChange msg) {
		Player player = players.get(msg.playerId);
		if(player != null) player.setMovementState(msg);
	}
	
	public synchronized Player getPlayerById(int id){
		return players.get(id);
	}

	public synchronized void setStatus(String status) {
		if(hud != null) hud.setStatus(status);
	}

	public synchronized void addBullet(PlayerShoots playerShoots) {
		if (isClient && playerShoots.playerID == playerLocal.getID()) {
			// tell others I shot
			client.sendMessage(playerShoots);
		}
		bullets.add(new Bullet(textureBullet, playerShoots.playerID, playerShoots.position, playerShoots.baseVelocity, playerShoots.direction, maxPosition));
	}
	
	private synchronized void addAsteroid(Vector2 position, float rotation){
		
		if(position.x < maxPosition.x * 0.6 && position.x > maxPosition.x * 0.3){
			asteroids.add(new Asteroid(textureAsteroid, textureAsteroidGold, position, rotation, 1));
		}
		else{
			asteroids.add(new Asteroid(textureAsteroid, textureAsteroidGold, position, rotation, 0));
		}
		
	
	}
	
	private synchronized void addAsteroid(AsteroidData asteroidData){
		if(asteroidData.position.x < maxPosition.x * 0.6 && asteroidData.position.x > maxPosition.x * 0.3){
			asteroids.add(new Asteroid(textureAsteroid, textureAsteroidGold, asteroidData.position, asteroidData.rotation, 1));
		}
		else {
		asteroids.add(new Asteroid(textureAsteroid, textureAsteroidGold, asteroidData.position, asteroidData.rotation, 0));
		}
	}
	
	public synchronized void generateMap(int asteroidQuantity){
		
//		random.setSeed(seed);
		// Generate asteroids
		int randomX = 0;
		int randomY = 0;
		int loopExit = 0;
		while(asteroids.size() < asteroidQuantity && loopExit < 1000){
			loopExit++;
			randomX = random.nextInt((int) maxPosition.x - textureAsteroid.getRegionWidth()-100) + 100;
			randomY = random.nextInt((int) maxPosition.y - textureAsteroid.getRegionHeight()-100) + 100;
			Rectangle box = new Rectangle(randomX , randomY, textureAsteroid.getRegionWidth(), textureAsteroid.getRegionHeight());
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
	
	public synchronized GameMapData getStateData(){
		List<AsteroidData> asteroidDatas = new ArrayList<AsteroidData>();
		for(Asteroid asteroid : asteroids){
			asteroidDatas.add(asteroid.getStateData());
		}
		return new GameMapData(asteroidDatas);
	}
	
	public synchronized void setStateData(GameMapData gameMapData){
		if(asteroids.size() > 0){
			Log.error("GameMap setStateData called more than once?");
		}else{
			// Make asteroids from server data
			for(AsteroidData asteroidData : gameMapData.asteroidDatas){
				addAsteroid(asteroidData);
			}
		}
	}

	public synchronized void resize(int width, int height) {
		cam.setToOrtho(false, width, height);
		hud.resize(width, height);
	}

	public synchronized void onMsgPlayerWasHit(PlayerWasHit msg) {
		Player hitter = players.get(msg.playerIdHitter);
		Player victim = players.get(msg.playerIdVictim);
		// give hitter points
		if(hitter != null && victim != null){
			victim.hit(msg.damage, msg.playerIdHitter);
			hitter.addScore(1);
			if(hitter == playerLocal){
				setStatus("You hit "+victim.getName()+"!");
			}else if(victim == playerLocal){
				setStatus(hitter.getName()+" hit you!");
			}else{
				setStatus(hitter.getName()+" hit "+victim.getName()+".");
			}
		}
		else{
			Log.error("PlayerWasHit msg referred to invalid players");
		}
	}
	
	public synchronized void clientSendMessage(Object msg){
		client.sendMessage(msg);
	}
	
	public synchronized void serverSendMessage(Object msg){
		server.sendMessage(msg);
	}
	
	public synchronized GameSounds gameSounds(){
		return gameSounds;
	}

	public synchronized void removeAsteroid(Vector2 position) {
		// Remove asteroids
		for (int i = 0; i < asteroids.size(); i++) {
			Asteroid asteroid = asteroids.get(i);
			if (asteroid.getPosition().equals(position)){
				logInfo("asteroid hit from server");
				asteroid.destroy();
				asteroids.remove(i);
			}
		}
	}
}
