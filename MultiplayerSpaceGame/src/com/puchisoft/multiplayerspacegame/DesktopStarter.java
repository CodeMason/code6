package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.puchisoft.multiplayerspacegame.screen.WaoGame;

public class DesktopStarter {
	public static void main(String[] args) {
		new LwjglApplication(new WaoGame(), "Multiplayer Space Game", 800, 600, false);
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
	}
}
