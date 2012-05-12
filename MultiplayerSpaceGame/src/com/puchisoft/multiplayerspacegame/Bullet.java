package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
	private static final long TIME_TO_LIVE =  2500 * 1000000L; // nanosec
	
	private int playerID;
	
	private Vector2 position;	
	private Vector2 velocity;
	
	private Vector2 maxPosition;
	
	private final float speed = 15.0f;

	private Sprite sprite;
	
	private long expirationTime = System.nanoTime() + TIME_TO_LIVE;
	private boolean destroyed = false;
	

	
	public Bullet(TextureRegion texture, int playerID, Vector2 position, Vector2 baseVelocity, Vector2 direction, Vector2 maxPosition) {
		this.playerID = playerID;
		this.position = position;
		
		this.sprite = new Sprite(texture);
		this.sprite.setRotation(direction.angle());
		this.sprite.setPosition(position.x, position.y);
		
		this.velocity = baseVelocity.add(direction.nor().mul(speed));
		this.maxPosition = maxPosition;
		
	}
	
	private void move(float delta) {
		position.add(velocity.tmp().mul(delta*60));
		this.sprite.setPosition(position.x, position.y);
	}
	
	private boolean collision(){
		if(position.x < 0 || position.x > maxPosition.x - sprite.getWidth()){
			destroy();
			return true;
		}	
		else if(position.y < 0 || position.y > maxPosition.y - sprite.getHeight()){
			destroy();
			return true;
		}
		return false;
	}
	
	public void destroy(){
		destroyed = true;
	}
	
	public boolean destroyed(){
		return destroyed || (System.nanoTime() > expirationTime);
	}
	
	public void update(float delta){
		this.move(delta);
		collision();
	}
	
	public void render(SpriteBatch spriteBatch) {
		this.sprite.draw(spriteBatch);
	}

	public int getPlayerID() {
		return playerID;
	}
	
	public Rectangle getBoundingRectangle(){
		return sprite.getBoundingRectangle();
	}
}
