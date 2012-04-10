package com.puchisoft;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.net.Network.MovementChange;
import com.puchisoft.net.Network.PlayerJoinLeave;
import com.puchisoft.net.Network;
import com.puchisoft.net.WaoClient;
import com.puchisoft.net.WaoServer;


public class WalkingAroundOnline implements ApplicationListener{

	private SpriteBatch spriteBatch;
	private BitmapFont font;
	
	private String status="Loading";
	
	Map<Integer,Player> players = new HashMap<Integer,Player>();
	
	private Player playerLocal;
	
	private Texture texturePlayer;	
	
	private WaoServer server;
	private WaoClient client;
	
	private FPSLogger fps = new FPSLogger();
	
	@Override
	public void create() {
		font = new BitmapFont();
		font.setColor(Color.GREEN);
		font.setScale(1.2f);
		
		texturePlayer = new Texture(Gdx.files.internal("data/player.png"));
		playerLocal = new Player(texturePlayer, new Vector2(50, 50));
		
		spriteBatch = new SpriteBatch();
		
		//Connect
		client = new WaoClient(this);
		client.connect("puchisoft.servegame.com", Network.port);
	}

	@Override
	public void dispose() {
		client.shutdown();
		if(server !=null) server.shutdown();
		texturePlayer.dispose();
		spriteBatch.dispose();
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void render() {
		if (Gdx.input.isKeyPressed(Keys.G)) {
			if(server == null){
				//Start server
				Log.info("Starting server...");
				try {
					server = new WaoServer();
				} catch (IOException e) {
					e.printStackTrace();
					Log.error("Can't start server. Already running?");
				}
				client.connectLocal();
			}
		}
		
		spriteBatch.begin();
		spriteBatch.setColor(Color.WHITE);
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		font.draw(spriteBatch, status, 5, font.getLineHeight());
		
		if(playerLocal.handleInput()){
			Log.info("changed input");
			client.sendMessage(playerLocal.getMovementState());
		}
		
		for(Map.Entry<Integer, Player> playerEntry: players.entrySet()){
			playerEntry.getValue().render(spriteBatch);
		}
				
		spriteBatch.end();
		fps.log();
	}

	@Override
	public void resize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	// TODO may only happen once...
	public void setNetworkId(int id){
		if(players.isEmpty()){
			this.playerLocal.setId(id);
			players.put(id,playerLocal);
		}else{
			Log.error("setNetworkId called twice?");
		}
	}

	public void addPlayer(PlayerJoinLeave msg) {
		Log.info("add player");
		Player newPlayer = new Player(texturePlayer, new Vector2(50, 50));
		newPlayer.setId(msg.playerId);
		players.put(msg.playerId, newPlayer);
	}
	
	public void removePlayer(PlayerJoinLeave msg){
		Log.info("remove player");
		players.remove(msg.playerId);
	}

	public void playerMoved(MovementChange msg) {
		Player player = players.get(msg.playerId);
		player.setMovementState(msg);
		
	}

}
