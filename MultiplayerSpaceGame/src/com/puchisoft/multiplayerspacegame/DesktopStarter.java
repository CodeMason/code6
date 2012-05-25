package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.puchisoft.multiplayerspacegame.screen.WaoGame;

public class DesktopStarter {
	public static void main(String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Multiplayer Space Game";
		config.width = 800;
		config.height = 600;
		config.useCPUSynch = false; // Doesn't work on some graphics cards in windowed mode... but looks way better; makes two instances of game run poorly
		
//		config.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width;
//		config.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height;
//		config.useCPUSynch = false;
//		config.resizable = false;
//		config.fullscreen = true;
		
		new LwjglApplication(new WaoGame(), config);
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
	}
}
