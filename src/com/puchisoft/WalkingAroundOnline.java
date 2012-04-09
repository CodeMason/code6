package com.puchisoft;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class WalkingAroundOnline implements ApplicationListener{

	SpriteBatch spriteBatch;
	BitmapFont font;
	
	Player player;
	Texture texturePlayer;	
	
	@Override
	public void create() {
		font = new BitmapFont();
		font.setColor(Color.GREEN);
		font.setScale(1.2f);
		
		texturePlayer = new Texture(Gdx.files.internal("data/player.png"));
		player = new Player(texturePlayer, new Vector2(50, 50));
		
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
		spriteBatch.begin();
		spriteBatch.setColor(Color.WHITE);
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		font.draw(spriteBatch, "Hello World", 5, font.getLineHeight());
		
		player.render(spriteBatch);
				
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
