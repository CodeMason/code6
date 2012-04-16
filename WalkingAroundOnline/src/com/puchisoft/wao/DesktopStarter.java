package com.puchisoft.wao;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.puchisoft.wao.screen.WaoGame;


public class DesktopStarter {
	public static void main(String[] args){
		new LwjglApplication(new WaoGame(), "Walking Around Online", 800, 600, false);
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
	}
}
