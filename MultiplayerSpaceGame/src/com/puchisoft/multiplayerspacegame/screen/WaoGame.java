package com.puchisoft.multiplayerspacegame.screen;

import com.badlogic.gdx.Game;

public class WaoGame extends Game {
	@Override
	public void create () {
//		Gdx.graphics.setVSync(true);
		setScreen(new ScreenMenu(this));
	}
}
