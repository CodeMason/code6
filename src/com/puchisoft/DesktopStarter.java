package com.puchisoft;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class DesktopStarter {
	public static void main(String[] args){
		new LwjglApplication(new WalkingAroundOnline(), "Walking Around Online", 800, 600, false);
//		new JoglApplication(new WalkingAroundOnline(), "Game", 640, 480, false);
	}
}
