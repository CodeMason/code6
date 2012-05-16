package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.puchisoft.multiplayerspacegame.net.Network.AsteroidData;

public class Asteroid {
	
	private static final float BOUNDINGBOX_REDUCTION = 0.17f; // percentage
	private Sprite sprite;
	private Vector2 position;
	private Rectangle boundingRectangle = new Rectangle();
	private int type; //0 = normal, 1 = gold
	
	public boolean destroyed = false;

	public Asteroid(TextureRegion texture,TextureRegion textureGold,  Vector2 position, float rotation, int type) {
		this.position = position;
		this.type = type;
		this.sprite = new Sprite(type == 0 ? texture: textureGold);
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
	
	public AsteroidData getStateData(){
		return new AsteroidData(position,sprite.getRotation());
	}
	
	public void destroy(){
		destroyed = true;
	}
	
}
