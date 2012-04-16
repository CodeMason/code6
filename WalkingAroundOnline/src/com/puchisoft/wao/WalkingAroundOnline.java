package com.puchisoft.wao;

import java.io.IOException;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.wao.net.Network;
import com.puchisoft.wao.net.WaoClient;
import com.puchisoft.wao.net.WaoServer;


public class WalkingAroundOnline implements ApplicationListener{

	private HUD hud;
	private GameMap map;
	
	private WaoServer server;
	private WaoClient client;
	
	private FPSLogger fps = new FPSLogger();
	
	@Override
	public void create() {
		
		hud = new HUD();
		map = new GameMap(hud);
		
		client = new WaoClient(map);
		client.connect("puchisoft.servegame.com", Network.port);
	}

	@Override
	public void render() {
		if (Gdx.input.isKeyPressed(Keys.G) || Gdx.input.isKeyPressed(Keys.MENU) || Gdx.input.isTouched(2)) {
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
		
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		map.render();
		hud.render();
		
		fps.log();
	}
	
	@Override
	public void resize(int width, int height) {
		map.cam.setToOrtho(false, width, height);
	}

	@Override
	public void dispose() {
		client.shutdown();
		if(server !=null) server.shutdown();
		map.dispose();
		hud.dispose();
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void pause() {
		
	}
}
