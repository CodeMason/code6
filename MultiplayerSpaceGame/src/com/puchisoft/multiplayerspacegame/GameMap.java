package com.puchisoft.multiplayerspacegame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.multiplayerspacegame.net.Network.MovementChange;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerJoinLeave;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerShoots;
import com.puchisoft.multiplayerspacegame.net.WaoClient;

public class GameMap {
	public OrthographicCamera cam;
	private SpriteBatch spriteBatch;

	private Map<Integer, Player> players = new HashMap<Integer, Player>();
	private List<Bullet> bullets = new ArrayList<Bullet>();

	private Player playerLocal;

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

	private Moon moonChase;	
	
	public GameMap(HUD hud) {
		this.hud = hud;
		Gdx.files.internal("data/background.png");
		textureBg = new Texture(Gdx.files.internal("data/background.png"));

		texturePlayer = new TextureRegion(new Texture(Gdx.files.internal("data/player.png")), 0, 0, 42, 32);
		textureBullet = new TextureRegion(new Texture(Gdx.files.internal("data/bullet.png")), 0, 0, 32, 6);
		texturemoon = new Texture(Gdx.files.internal("data/moon.png"));
		textureChasemoon = new Texture(Gdx.files.internal("data/moonchase.png"));
			
		maxPosition = new Vector2(textureBg.getWidth() * tilesCount, textureBg.getHeight() * tilesCount);

		moonChase = new Moon(textureChasemoon, new Vector2(50, 50));
		
		this.cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		sizemoon = new Vector2(texturemoon.getWidth(), texturemoon.getHeight());
		
		spriteBatch = new SpriteBatch(); //

	}

	public void render(float delta) {
		if (client == null || playerLocal == null) {
			return;
		}
		
		update(delta);
		render();
				
		
	}
	
	private void update(float delta){
		// Zooming
		if (Gdx.input.isKeyPressed(Keys.Q)){
			this.cam.zoom = Math.max(this.cam.zoom - 1.0f * delta , 0.3f);
		}else if (Gdx.input.isKeyPressed(Keys.E)){
			this.cam.zoom = Math.min(this.cam.zoom + 1.0f * delta, 10.0f);
		}
		else if (Gdx.input.isTouched(1)){
			this.cam.zoom = Math.min(Math.max(this.cam.zoom + (Gdx.input.getDeltaX(1) * 0.5f * delta), 0.3f), 10.0f);
		}
		
		this.cam.position.set(playerLocal.getDesiredCameraPosition(this.cam.position, delta));
		cam.update();

		// Handle local input and sent over network if changed
		if (playerLocal.handleInput(delta)) {
			Log.info("changed input");
			client.sendMessage(playerLocal.getMovementState());
		}

		// Update Players
		for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
			playerEntry.getValue().update(delta);
		}

		// Update Bullets
		for (int i = 0; i < bullets.size(); i++) {
			Bullet bulletCur = bullets.get(i);
			bullets.get(i).update(delta);
			// Collision with Players
			for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
				Player playerCur = playerEntry.getValue();
				if(playerCur.getID() != bulletCur.getPlayerID() && playerCur.getBoundingRectangle().overlaps(bulletCur.getBoundingRectangle())){
					bulletCur.destroy();
					playerCur.hit();
					if(playerCur == playerLocal){
						Gdx.input.vibrate(300);
						client.sendMessage(playerLocal.getMovementState());
					}
				}
			}
			
			// Remove impacted bullets
			if (bullets.get(i).destroyed)
				bullets.remove(i);
		}
		
		Player closestPlayer = playerLocal;
		float moonChaseDist = 999999999;
		
		for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
			float currentdist = playerEntry.getValue().position.dst2(moonChase.position);
			if (currentdist < moonChaseDist) {
				moonChaseDist = currentdist;
				closestPlayer = playerEntry.getValue();
			}
		}
		
		moonChase.position.lerp(closestPlayer.position, 0.01f);
		
		
	}
	
	private void render(){
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
		
		// Render Players
		for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
			playerEntry.getValue().render(spriteBatch);
		}
		// Render Bullets
		for (int i = 0; i < bullets.size(); i++) {
			bullets.get(i).render(spriteBatch);
		}
		
		moonChase.render(spriteBatch);
		
		spriteBatch.end();
	}

	public void dispose() {
		texturePlayer.getTexture().dispose();
		textureBg.dispose();
		spriteBatch.dispose();
	}

	public void onConnect(WaoClient client, Color color) {
				
		if (this.client == null) {
			this.client = client;
			Moon moonLocal = new Moon(texturemoon, new Vector2(50, 50));
			playerLocal = new Player(texturePlayer, new Vector2(50, 50), maxPosition, this, color, moonLocal);
			this.playerLocal.setId(client.id);
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
		Player newPlayer = new Player(texturePlayer, new Vector2(50, 50), maxPosition, this, msg.color, new Moon(texturemoon, new Vector2(50, 50)));
		newPlayer.setId(msg.playerId);
		players.put(msg.playerId, newPlayer);

		// tell people where I am again
		// TODO server should remember this and tell others based on emulating players movements locally
		client.sendMessage(playerLocal.getMovementState());
	}

	public void removePlayer(PlayerJoinLeave msg) {
		Log.info("remove player");
		players.remove(msg.playerId);
	}

	public void playerMoved(MovementChange msg) {
		Player player = players.get(msg.playerId);
		player.setMovementState(msg);

	}

	public void setStatus(String status) {
		hud.setStatus(status);
	}

	public void addBullet(PlayerShoots playerShoots) {
		if (playerShoots.playerID == playerLocal.getID()) {
			// tell others I shot
			client.sendMessage(playerShoots);
		}
		bullets.add(new Bullet(textureBullet, playerShoots.playerID, playerShoots.position, playerShoots.baseVelocity, playerShoots.direction, maxPosition));
	}

}
