package com.puchisoft;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class WalkingAroundOnline implements ApplicationListener{

	SpriteBatch spriteBatch;
	BitmapFont font;
	
	Texture texturePlayer;
	Vector2 playerPosition = new Vector2(50, 50);
	
	Vector2 textPosition = new Vector2(50, 200);
	
	@Override
	public void create() {
		font = new BitmapFont();
		font.setColor(Color.GREEN);
		font.setScale(2.0f);
		
		texturePlayer = new Texture(Gdx.files.internal("data/player.png"));
		
		spriteBatch = new SpriteBatch();
	}

	@Override
	public void dispose() {
		texturePlayer.dispose();
		spriteBatch.dispose();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() {
		
		if (Gdx.input.isKeyPressed(Keys.W)) {
			playerPosition.y += 3;
		}
		else if(Gdx.input.isKeyPressed(Keys.S)) {
			playerPosition.y -= 3;
		}
		if (Gdx.input.isKeyPressed(Keys.A)) {
			playerPosition.x -= 3;
		}
		else if(Gdx.input.isKeyPressed(Keys.D)) {
			playerPosition.x += 3;
		}
		
		spriteBatch.begin();
		spriteBatch.setColor(Color.WHITE);
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		font.draw(spriteBatch, "Hello World", (int)textPosition.x, (int)textPosition.y);
		
		spriteBatch.draw(texturePlayer, playerPosition.x, playerPosition.y, 0, 0, texturePlayer.getWidth(),
				texturePlayer.getHeight());
		spriteBatch.end();
	}

	@Override
	public void resize(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

}
