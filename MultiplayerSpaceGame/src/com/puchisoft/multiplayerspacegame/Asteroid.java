package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Asteroid {
	
	private Sprite sprite;
	
	public Asteroid(TextureRegion texture, Vector2 position) {
		this.sprite = new Sprite(texture);
		this.sprite.setPosition(position.x, position.y);
	}

	public void render(SpriteBatch spriteBatch) {
		sprite.draw(spriteBatch);
		
	}

	public Vector2 getPosition() {
		return new Vector2(this.sprite.getX(),this.sprite.getY());
	}
}
