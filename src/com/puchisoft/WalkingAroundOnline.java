package com.puchisoft;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class WalkingAroundOnline implements ApplicationListener{

	SpriteBatch spriteBatch;
	BitmapFont font;
	Vector2 textPosition = new Vector2(50, 200);
	
	@Override
	public void create() {
		font = new BitmapFont();
		font.setColor(Color.GREEN);
		font.setScale(2.0f);
		spriteBatch = new SpriteBatch();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render() {
		spriteBatch.begin();
		spriteBatch.setColor(Color.WHITE);
		font.draw(spriteBatch, "Hello World", (int)textPosition.x, (int)textPosition.y);
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
