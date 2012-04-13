package com.puchisoft.wao;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;


public class DesktopStarter {
	public static void main(String[] args){
		new LwjglApplication(new WalkingAroundOnline(), "Walking Around Online", 800, 600, false);
	}
}
