
package com.puchisoft.multiplayerspacegame.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ScreenMenu extends ScreenCore {
	Texture title;
	SpriteBatch batch;
	float time = 0;

	public ScreenMenu (Game game) {
		super(game);
	}

	@Override
	public void show () {
		title = new Texture(Gdx.files.internal("data/load.png"));
		batch = new SpriteBatch();
	}

	@Override
	public void render (float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(title, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		
		if (time > 0) {
			game.setScreen(new ScreenGame(game));
			return;
		}
		time += delta;
	}

	@Override
	public void hide () {
		batch.dispose();
		title.dispose();
	}
}
