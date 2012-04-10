package com.puchisoft;

import java.io.IOException;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.net.WaoClient;
import com.puchisoft.net.WaoServer;


public class WalkingAroundOnline implements ApplicationListener{

	private SpriteBatch spriteBatch;
	private BitmapFont font;
	
	private String status="Hello World";
	
	private Player player;
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
		player = new Player(texturePlayer, new Vector2(50, 50));
		
		spriteBatch = new SpriteBatch();
		
		//Connect
		client = new WaoClient(this);
		client.connectLocal();
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
		
		player.render(spriteBatch);
				
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

}
