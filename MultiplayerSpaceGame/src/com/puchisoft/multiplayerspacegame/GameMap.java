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
import com.puchisoft.multiplayerspacegame.net.Network.GameMapData;
import com.puchisoft.multiplayerspacegame.net.Network.MovementState;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerJoinLeave;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerShoots;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerSpawns;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerWasHit;
import com.puchisoft.multiplayerspacegame.net.Network.RoundEnd;
import com.puchisoft.multiplayerspacegame.net.Network.RoundStart;
import com.puchisoft.multiplayerspacegame.net.WaoClient;
import com.puchisoft.multiplayerspacegame.net.WaoServer;

public class GameMap {
	private static final int GOAL_SCORE = 10;
	private static final int ASTEROID_QUANITY = 100;
	private static final long ROUND_OVER_DELAY = 10000 * 1000000L; // nanosec
	private static final int BG_TILE_COUNT = 3;
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

	private Vector2 maxPosition;

	private WaoClient client; // only if I'm the client
	private WaoServer server; // only if I'm internal to the server
	private HUD hud;
	private TextureRegion textureAsteroid;
	private TextureRegion textureAsteroidGold;
	
	private GameSounds gameSounds;
	
	private long timeRoundBegins = 0; // ms
	
	boolean isClient;
	private boolean roundOver = false;

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
		
		generateMap(ASTEROID_QUANITY);
	}

	private void initCommon(){
		textureBg = new Texture(Gdx.files.internal("data/background.png"));

		texturePlayer = new TextureRegion(new Texture(Gdx.files.internal("data/player.png")), 0, 0, 42, 32);
		textureBullet = new TextureRegion(new Texture(Gdx.files.internal("data/bullet.png")), 0, 0, 32, 6);
		textureAsteroid = new TextureRegion(new Texture(Gdx.files.internal("data/asteroid.png")), 0, 0, 64, 64);
		textureAsteroidGold = new TextureRegion(new Texture(Gdx.files.internal("data/asteroid_gold.png")), 0, 0, 64, 64);
		
		maxPosition = new Vector2(textureBg.getWidth() * BG_TILE_COUNT, textureBg.getHeight() * BG_TILE_COUNT);
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
					logInfo("Player touched asteroid");
					if(!isClient){
//						playerCur.hit(1, -1);
						PlayerWasHit msg = new PlayerWasHit(playerCur.getID(),-1, 1); // hit by non-Player
						this.onPlayerWasHit(msg);
						server.sendMessage(msg);
					}
				}
			}
			
			// Spawn dead players (sends a message to clients)
			if(!isClient && !roundOver) playerCur.spawnIfAppropriate();
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
					if(!isClient && !roundOver){ // no getting hit once round ends
						PlayerWasHit msg = new PlayerWasHit(playerCur.getID(),bulletCur.getPlayerID(), 45);
						this.onPlayerWasHit(msg);
						server.sendMessage(msg);
					}
				}
			}
			// Collision with Asteroids
			for(Asteroid asteroid : asteroids){
				if(bulletCur.getBoundingRectangle().overlaps(asteroid.getBoundingRectangle())){
					bulletCur.destroy();
					// Only server makes call whether asteroids are killed
//					if(!isClient){
//						server.sendMessage(new AsteroidWasHit(asteroid.getPosition()));
//						asteroid.destroy();
//					}
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
		
		//Check for new Round - Server Only
		if(!isClient && roundOver && System.nanoTime() > timeRoundBegins){
			generateMap(ASTEROID_QUANITY);
			RoundStart msg = new RoundStart();
			onRoundStart(msg);
			server.sendMessage(msg);
			
			// Respawn everyone
			for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
				Player playerCur = playerEntry.getValue();
				playerCur.spawn(); // make their position reset, regardless of whether they are dead or how healthy they were
			}
		}
	}
	
	// Client and Server
	public synchronized void onRoundStart(RoundStart msgRoundStart) {
		logInfo("Round Start");
		roundOver = false;
		// Reset everyone's score
		for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
			Player playerCur = playerEntry.getValue();
			playerCur.setScore(0);
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
		for (int i = 0; i < BG_TILE_COUNT; i++) {
			for (int j = 0; j < BG_TILE_COUNT; j++) {
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
			// TODO Server should spawn localPlayer too
			playerLocal = new Player(texturePlayer, new Vector2(50, 50), maxPosition, this, color, true, 100);
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

	public synchronized Player addPlayer(PlayerJoinLeave msg) {
		Log.debug("add player");
		Player newPlayer = new Player(texturePlayer, new Vector2(50, 50), maxPosition, this, msg.color, false, msg.health);
		newPlayer.setId(msg.playerId);
		newPlayer.setName(msg.name);
		newPlayer.addScore(msg.score);
		players.put(msg.playerId, newPlayer);
		return newPlayer;
	}

	public synchronized void removePlayer(PlayerJoinLeave msg) { // synchronized
		logInfo("remove player");
		players.remove(msg.playerId);
	}

	public synchronized void playerMoved(MovementState msg) {
		Player player = players.get(msg.playerId);
		if(player != null) player.setMovementState(msg);
	}
	
	public synchronized Player getPlayerById(int id){
		return players.get(id);
	}

	public synchronized void setStatus(String status) {
		if(hud != null) hud.setStatus(status);
	}
	public synchronized void setStatusCenter(String status) {
		if(hud != null) hud.setStatusCenter(status);
	}
	
	// returns of move was valid
	public synchronized boolean onMsgPlayerShoots(PlayerShoots playerShoots){
		Player player = getPlayerById(playerShoots.playerID);
		logInfo(playerShoots.playerID+" shoots");
		if(player != null){
//			player.setPosition(playerShoots.position);
//			player.setDirection(playerShoots.direction);
//			player.setVelocity(playerShoots.baseVelocity); // fixme
			if(!player.shoot()){
				logInfo(player.getName()+" tried to Cheat with bullet spam!");
				return false;
			}
		}else{
			logInfo("onMsgPlayerShoots has unknown playerId");
		}
		return true;
	}

	public synchronized void addBullet(PlayerShoots playerShoots) {
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
	
	// Run only by server; Needs to clean up; Client will then setState, which also needs to clean up
	public synchronized void generateMap(int asteroidQuantity){
		
		// Reset map
		asteroids.clear();
		
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
		
		server.sendMessage(getStateData());
	}
	
	public synchronized GameMapData getStateData(){
		List<AsteroidData> asteroidDatas = new ArrayList<AsteroidData>();
		for(Asteroid asteroid : asteroids){
			asteroidDatas.add(asteroid.getStateData());
		}
		return new GameMapData(asteroidDatas, roundOver);
	}
	
	public synchronized void setStateData(GameMapData gameMapData){
		setStatusCenter("");
		roundOver = gameMapData.roundOver;
		
		asteroids.clear();
		// Make asteroids from server data
		for(AsteroidData asteroidData : gameMapData.asteroidDatas){
			addAsteroid(asteroidData);
		}
	}

	public synchronized void resize(int width, int height) {
		cam.setToOrtho(false, width, height);
		hud.resize(width, height);
	}

	public synchronized void onPlayerWasHit(PlayerWasHit msg) {
		Player hitter = players.get(msg.playerIdHitter);
		Player victim = players.get(msg.playerIdVictim);
		// give hitter points
		if(victim != null){ // TODO Won't work if there is not hitter
			boolean victimDied = victim.hit(msg.damage, msg.playerIdHitter); // might be -1 for non-Player
			if(hitter != null){
				hitter.addScore(victimDied ? 2 : 1);
				if(!isClient){
					if(hitter.getScore() >= GOAL_SCORE){
						RoundEnd msgRE = new RoundEnd(hitter.getID());
						this.onRoundEnd(msgRE);
						server.sendMessage(msgRE);
					}
				}else{
					if(hitter == playerLocal){
						setStatus("You hit "+victim.getName()+"!");
					}else if(victim == playerLocal){
						setStatus(hitter.getName()+" hit you!");
					}else{
						setStatus(hitter.getName()+" hit "+victim.getName()+".");
					}
				}
			}
		}
		else{
			Log.error("PlayerWasHit msg referred to invalid players");
		}
	}
	
	public synchronized void onRoundEnd(RoundEnd msg) {
		logInfo("Round over");
		roundOver = true;
		Player winner = players.get(msg.winnerID);
		if(winner != null){
			timeRoundBegins = System.nanoTime() + ROUND_OVER_DELAY;
			setStatusCenter("Round Over! "+winner.getName()+" is the winner.");
			killAllPlayersExcept(winner);
		}
	}
	
	private void killAllPlayers(){
		killAllPlayersExcept(null);
	}
	
	private void killAllPlayersExcept(Player survivor){
		for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
			Player playerCur = playerEntry.getValue();
			if(playerCur != survivor){
				// FIXME shouldn't change your score by you losing
				playerCur.hit(9999, survivor != null ? survivor.getID() : -1); // make everyone else look at winner
			}
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

	public synchronized void onPlayerSpawn(PlayerSpawns msg) {
		Player spawner = players.get(msg.playerId);
		if(spawner != null){
			spawner.spawn(msg);
		}
	}
}
