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
	private BitmapFont fontBig;
	private String status = "Loading";
	private String statusCenter = "";
	private Player playerLocal;
	

	public HUD() {
		font = new BitmapFont();
		font.setColor(Color.GREEN);
		fontBig = new BitmapFont();
		fontBig.setColor(Color.toFloatBits(255, 215, 0, 255));
		fontBig.setScale(2.0f);

		spriteBatchStationary = new SpriteBatch();
		this.cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	public void render() {
		spriteBatchStationary.setProjectionMatrix(cam.combined);
		spriteBatchStationary.begin();

		// Bottom left status
		font.draw(spriteBatchStationary, status, 5, font.getLineHeight());
		
		// Center status
		fontBig.draw(spriteBatchStationary, statusCenter, 30, Gdx.graphics.getHeight()*0.5f);
		
		// HUD
		if(playerLocal != null){
			font.draw(spriteBatchStationary, "Score: "+playerLocal.getScore(), 5, Gdx.graphics.getHeight() - font.getXHeight());
			if(playerLocal.getHealth() > 0)
				font.draw(spriteBatchStationary, "Health: "+(int)playerLocal.getHealth(), 5, Gdx.graphics.getHeight() - font.getXHeight()*4);
			else
				font.draw(spriteBatchStationary, "Dead", 5, Gdx.graphics.getHeight() - font.getXHeight()*4);
		}
		spriteBatchStationary.end();
	}

	public void setStatus(String status) {
		this.status = status;
	}
	public void setStatusCenter(String status) {
		this.statusCenter = status;
	}
	
	public void setPlayerLocal(Player playerLocal) {
		this.playerLocal = playerLocal;
	}

	public void dispose() {
		spriteBatchStationary.dispose();
	}

	public void resize(int width, int height) {
		cam.setToOrtho(false, width, height);
		
	}
}
