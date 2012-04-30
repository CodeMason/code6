package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
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

	public Rectangle getBoundingRectangle() {
		float scaleX = sprite.getBoundingRectangle().width * 0.1f;
		float scaleY = sprite.getBoundingRectangle().height * 0.1f;
		Rectangle boundingRectangle = new Rectangle(sprite.getBoundingRectangle().x+scaleX,sprite.getBoundingRectangle().y+scaleY,sprite.getBoundingRectangle().width-(scaleX * 2), sprite.getBoundingRectangle().height-(scaleY * 2));
		
		return boundingRectangle;
	}
	
}
