package com.puchisoft.wao;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.wao.net.Network.MovementChange;
import com.puchisoft.wao.net.Network.PlayerJoinLeave;
import com.puchisoft.wao.net.WaoClient;

public class GameMap {
	public OrthographicCamera cam;
	private SpriteBatch spriteBatch;
	
	
	private Map<Integer,Player> players = new HashMap<Integer,Player>();
	
	private Player playerLocal;
	
	private Texture texturePlayer;
	private Texture textureBg;
	private int tilesCount = 5;
	
	private Vector2 maxPosition;
	
	private WaoClient client;
	private HUD hud;
	
	public GameMap(HUD hud) {
		this.hud= hud;
		Gdx.files.internal("data/background.png");
		textureBg = new Texture(Gdx.files.internal("data/background.png"));
		
		texturePlayer = new Texture(Gdx.files.internal("data/player.png"));
		
		maxPosition = new Vector2(textureBg.getWidth()*tilesCount, textureBg.getHeight()*tilesCount);
		playerLocal = new Player(texturePlayer, new Vector2(50, 50),maxPosition);
		
		this.cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.cam.position.set(playerLocal.position.x, playerLocal.position.y, 0);
		
		spriteBatch = new SpriteBatch();
		
	}
	
	public void render(){
		if(client == null){
			return;
		}
		this.cam.position.set(playerLocal.position.x, playerLocal.position.y, 0);
		cam.update();
		
		spriteBatch.setProjectionMatrix(cam.combined);
		spriteBatch.begin();
		spriteBatch.setColor(Color.WHITE);
		
		for(int i = 0; i < tilesCount; i++){
			for(int j = 0; j < tilesCount; j++){
				spriteBatch.draw(textureBg, textureBg.getWidth()*i, textureBg.getHeight()*j, 0, 0, textureBg.getWidth(), textureBg.getHeight());
			}
		}
		
		if(playerLocal.handleInput()){
			Log.info("changed input");
			client.sendMessage(playerLocal.getMovementState());
		}
		
		for(Map.Entry<Integer, Player> playerEntry: players.entrySet()){
			playerEntry.getValue().render(spriteBatch);
		}
				
		spriteBatch.end();
		
	}
	
	public void dispose(){
		texturePlayer.dispose();
		textureBg.dispose();
		spriteBatch.dispose();
	}

	// OnConnect
	public void setNetworkClient(WaoClient client) {
		
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
				maxPosition);
		newPlayer.setId(msg.playerId);
		players.put(msg.playerId, newPlayer);
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

}
