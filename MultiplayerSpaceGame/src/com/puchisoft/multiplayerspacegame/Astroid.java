package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Astroid {
	
	private Sprite sprite;
	
	public Astroid(TextureRegion texture, Vector2 position) {
		this.sprite = new Sprite(texture);
		this.sprite.setPosition(position.x, position.y);
	}

	public void render(SpriteBatch spriteBatch) {
		sprite.draw(spriteBatch);
		
	}
}
