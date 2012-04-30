package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class HUD {
	private SpriteBatch spriteBatchStationary;
	public OrthographicCamera cam;
	private BitmapFont font;
	private String status = "Loading";
	private int score = 0;
	

	public HUD() {
		font = new BitmapFont();
		font.setColor(Color.GREEN);
//		font.setScale(1.2f);

		spriteBatchStationary = new SpriteBatch();
		this.cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	public void render() {
		spriteBatchStationary.setProjectionMatrix(cam.combined);
		spriteBatchStationary.begin();
		font.draw(spriteBatchStationary, status, 5, font.getLineHeight());
		font.draw(spriteBatchStationary, "Score: "+score, 5, Gdx.graphics.getHeight() - font.getXHeight());
		spriteBatchStationary.end();
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setScore(int score){
		this.score  =score;
	}

	public void dispose() {
		spriteBatchStationary.dispose();
	}

	public void resize(int width, int height) {
		cam.setToOrtho(false, width, height);
		
	}
}
