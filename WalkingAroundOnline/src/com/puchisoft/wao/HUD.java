package com.puchisoft.wao;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class HUD {	
	private SpriteBatch spriteBatchStationary;
	private BitmapFont font;
	private String status="Loading";
	
	public HUD() {
		font = new BitmapFont();
		font.setColor(Color.GREEN);
		font.setScale(1.2f);
		
		spriteBatchStationary = new SpriteBatch();
	}
	
	public void render(float delta){
		spriteBatchStationary.begin();
		font.draw(spriteBatchStationary, status, 5, font.getLineHeight());
		spriteBatchStationary.end();
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void dispose(){
		spriteBatchStationary.dispose();
	}
}
