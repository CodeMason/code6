package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
	private int playerID;
	
	private Vector2 position;	
	private Vector2 velocity;
	
	public Vector2 maxPosition;
	
	final private float speed = 15.0f;

	private Sprite sprite;
	
	public boolean destroyed = false;

	
	public Bullet(TextureRegion texture, int playerID, Vector2 position, Vector2 baseVelocity, Vector2 direction, Vector2 maxPosition) {
		this.sprite = new Sprite(texture);
		this.sprite.setRotation(direction.angle());
		
		this.playerID = playerID;
		this.position = position;
		this.velocity = baseVelocity.add(direction.nor().mul(speed));
		this.maxPosition = maxPosition;
		
	}
	
	private void move(float delta) {
		position.add(velocity.tmp().mul(delta*60));
		this.sprite.setPosition(position.x, position.y);
	}
	
	private boolean collision(){
		if(position.x < 0 || position.x > maxPosition.x - sprite.getWidth()){
			return destroy();
		}	
		else if(position.y < 0 || position.y > maxPosition.y - sprite.getHeight()){
			return destroy();
		}
		return false;
	}
	
	public boolean destroy(){
		destroyed = true;
		return true;
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
