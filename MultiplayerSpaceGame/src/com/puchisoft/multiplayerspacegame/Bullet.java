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
		this.setPosition(position);
		this.setVelocity(baseVelocity.add(direction.nor().mul(speed)));
		this.maxPosition = maxPosition;
		
	}
	
	private void move(float delta) {
		getPosition().add(getVelocity().tmp().mul(delta*60));
		this.sprite.setPosition(getPosition().x, getPosition().y);
	}
	
	//Bounces bullets off sides five times then destroys
	int bulletCollision =0;
	private boolean collision(){
		if(getPosition().x < 0 || getPosition().x > maxPosition.x - sprite.getWidth()){
			if(bulletCollision < 4){
				velocity.x = velocity.x * -1;
				bulletCollision = bulletCollision + 1;
				}
				else return destroy();
		}	
		else if(getPosition().y < 0 || getPosition().y > maxPosition.y - sprite.getHeight()){
			if(bulletCollision < 5){
			velocity.y = velocity.y * -1;
			bulletCollision = bulletCollision + 1;
			}
			else return destroy();
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

	public Vector2 getPosition() {
		return position;
	}

	public void setPosition(Vector2 position) {
		this.position = position;
	}

	public Vector2 getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2 velocity) {
		this.velocity = velocity;
	}
}
