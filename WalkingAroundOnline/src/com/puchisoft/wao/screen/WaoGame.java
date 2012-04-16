package com.puchisoft.wao.screen;

import com.badlogic.gdx.Game;

public class WaoGame extends Game {
	@Override
	public void create () {
		setScreen(new ScreenMenu(this));
	}
}
