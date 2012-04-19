package com.puchisoft.multiplayerspacegame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.multiplayerspacegame.net.WaoClient;
import com.puchisoft.multiplayerspacegame.net.Network.MovementChange;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerJoinLeave;

public class GameMap {
	public OrthographicCamera cam;
	private SpriteBatch spriteBatch;
	
	
	private Map<Integer,Player> players = new HashMap<Integer,Player>();
	private List<Bullet> bullets = new ArrayList<Bullet>();
	
	private Player playerLocal;
	
	private TextureRegion texturePlayer;
	private TextureRegion textureBullet;
	private Texture textureBg;
	private int tilesCount = 3;
	
	private Vector2 maxPosition;
	
	private WaoClient client;
	private HUD hud;
	
	public GameMap(HUD hud) {
		this.hud= hud;
		Gdx.files.internal("data/background.png");
		textureBg = new Texture(Gdx.files.internal("data/background.png"));
		
		texturePlayer = new TextureRegion(new Texture(Gdx.files.internal("data/player.png")), 0, 0, 42, 32);
		textureBullet = new TextureRegion(new Texture(Gdx.files.internal("data/bullet.png")), 0, 0, 32, 6);
		
		maxPosition = new Vector2(textureBg.getWidth()*tilesCount, textureBg.getHeight()*tilesCount);
		playerLocal = new Player(texturePlayer, new Vector2(50, 50),maxPosition,this);
		
		this.cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		spriteBatch = new SpriteBatch(); //
		
	}
	
	public void render(float delta){
		if(client == null){
			return;
		}
		this.cam.position.set(playerLocal.getDesiredCameraPosition(this.cam.position, delta));
		cam.update();
		
		spriteBatch.setProjectionMatrix(cam.combined);
		spriteBatch.begin();
		spriteBatch.setColor(Color.WHITE);
		
		for(int i = 0; i < tilesCount; i++){
			for(int j = 0; j < tilesCount; j++){
				spriteBatch.draw(textureBg, textureBg.getWidth()*i, textureBg.getHeight()*j, 0, 0, textureBg.getWidth(), textureBg.getHeight());
			}
		}

		// Handle local input and sent over network if changed
		if(playerLocal.handleInput()){
			Log.info("changed input");
			client.sendMessage(playerLocal.getMovementState());
		}
		
		// Render Players
		for(Map.Entry<Integer, Player> playerEntry: players.entrySet()){
			playerEntry.getValue().render(spriteBatch, delta);
		}
		
		// Render Bullets
		for (int i = 0; i < bullets.size(); i++) {
			bullets.get(i).render(spriteBatch, delta);
			if (bullets.get(i).destroyed)
				bullets.remove(i);
		}

		spriteBatch.end();

	}

	public void dispose(){
		texturePlayer.getTexture().dispose();
		textureBg.dispose();
		spriteBatch.dispose();
	}

	public void onConnect(WaoClient client) {
		
		if (this.client == null) {
			this.client = client;
			this.playerLocal.setId(client.id);
			players.put(client.id, playerLocal);
			setStatus("Connected to "+client.remoteIP);
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
				maxPosition,this);
		newPlayer.setId(msg.playerId);
		players.put(msg.playerId, newPlayer);
		
		// tell people where I am again
		// TODO server should remember this and tell others
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

	public void addBullet(Player player, Vector2 position, Vector2 velocity,
			Vector2 direction) {
		bullets.add(new Bullet(textureBullet, position, velocity, direction, maxPosition));
		if(player == playerLocal ){
			// network code here
		}
	}

}