package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Asteroid {
	
	private static final float BOUNDINGBOX_REDUCTION = 0.17f; // percentage
	private Sprite sprite;
	Vector2 position;
	private Rectangle boundingRectangle = new Rectangle();

	
	public Asteroid(TextureRegion texture, Vector2 position, float rotation) {
		this.position = position;
		this.sprite = new Sprite(texture);
		this.sprite.setPosition(position.x, position.y);
		this.sprite.rotate(rotation);
	}

	public void render(SpriteBatch spriteBatch) {
		sprite.draw(spriteBatch);
		
	}

	public Vector2 getPosition() {
		return position;
	}
	
	public void setPosition(Vector2 position){
		this.position = position;
		sprite.setPosition(position.x, position.y);
	}

	public Rectangle getBoundingRectangle() {
		float scaleX = sprite.getBoundingRectangle().width * BOUNDINGBOX_REDUCTION;
		float scaleY = sprite.getBoundingRectangle().height * BOUNDINGBOX_REDUCTION;
		boundingRectangle.x = sprite.getBoundingRectangle().x+scaleX;
		boundingRectangle.y = sprite.getBoundingRectangle().y+scaleY;
		boundingRectangle.width = sprite.getBoundingRectangle().width-(scaleX * 2);
		boundingRectangle.height = sprite.getBoundingRectangle().height-(scaleY * 2);
		
		return boundingRectangle;
	}
	
}
