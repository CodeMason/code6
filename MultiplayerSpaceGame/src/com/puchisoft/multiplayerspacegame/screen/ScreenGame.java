package com.puchisoft.multiplayerspacegame.screen;

import java.io.IOException;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.multiplayerspacegame.GameMap;
import com.puchisoft.multiplayerspacegame.net.WaoClient;
import com.puchisoft.multiplayerspacegame.net.WaoServer;

public class ScreenGame extends ScreenCore {

	private GameMap map;

	private WaoServer server;
	private WaoClient client;
	private final boolean isHost;
	private final String ip;
	private String name;

	private FPSLogger fps = new FPSLogger();

	public ScreenGame(Game game, boolean isHost, String ip, String name) {
		super(game);
		this.isHost = isHost;
		if(!ip.isEmpty()){
			this.ip = ip;
		}else{
			this.ip = "localhost";
		}
		this.name = name;
	}

	@Override
	public void show() {
		
		Gdx.input.setCatchBackKey(true);

		client = new WaoClient(name);
		map = client.getMap();
		
		if(isHost){
			// Start server
			logInfo("Starting server...");
			try {
				server = new WaoServer();
				client.connectLocal();
			} catch (IOException e) {
				e.printStackTrace();
				logInfo("Can't start server. Already running?");
				game.setScreen(new ScreenMenu(game));
			}
		}else{
			client.connect(ip);
		}
	}

	private void logInfo(String string) {
		Log.info(string);
	}

	@Override
	public void render(float delta) {
		if (Gdx.input.isKeyPressed(Keys.ESCAPE) || Gdx.input.isKeyPressed(Keys.BACK)) {
			game.setScreen(new ScreenMenu(game));
		}

		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		map.update(delta);
		map.render();
		
		if(isHost){
			server.update(delta);
		}

		// emulate terrible fps
		// try {
		// Thread.sleep(30);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

		// Log.info(Float.toString(delta)+" "+Float.toString(1/delta)+" "+30f*
		// delta);
		 fps.log();
	}

	@Override
	public void resize(int width, int height) {
		map.resize(width,height);
	}

	@Override
	public void hide() {
		Gdx.input.setCatchBackKey(false);
		client.shutdown();
		if (server != null)
			server.shutdown();
		map.dispose();
	}

	@Override
	public void resume() {

	}

	@Override
	public void pause() {

	}
}
