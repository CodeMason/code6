package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.minlog.Log;

public class Bullet {
	private int playerID;
	
	private Vector2 position;	
	private Vector2 velocity;
	
	public Vector2 maxPosition;
	
	final private float speed = 15.0f;
	int bulletCollision =0;
	
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

	private boolean collision(){
		if(getPosition().x < 0 || getPosition().x > maxPosition.x - sprite.getWidth()){
			if(bulletCollision < 15){
				velocity.x = velocity.x * -1;
				bulletCollision = bulletCollision + 1;
				}
				else return destroy();
		}	
		else if(getPosition().y < 0 || getPosition().y > maxPosition.y - sprite.getHeight()){
			if(bulletCollision < 15){
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
	
	public void preventOverlap(Rectangle otherColRectangle, float delta) {
//		Log.info("revert...");
//		Log.info("P x"+position.x+" y"+position.y);
//		Log.info("PBB x" + getBoundingRectangle().x + " y" + getBoundingRectangle().y + " w" + getBoundingRectangle().width + " h" + getBoundingRectangle().height);
//		Log.info("OBB x" + otherColRectangle.x + " y" + otherColRectangle.y + " w" + otherColRectangle.width + " h" + otherColRectangle.height);
		
		float distY = Math.abs((getBoundingRectangle().y + getBoundingRectangle().height / 2) - (otherColRectangle.y + otherColRectangle.height / 2));
		float totalHeight = (getBoundingRectangle().height / 2 + otherColRectangle.height / 2);
		float overlapY = totalHeight - distY;
//		Log.info("O y" + overlapY);

		float distX = Math.abs((getBoundingRectangle().x + getBoundingRectangle().width / 2) - (otherColRectangle.x + otherColRectangle.width / 2));
		float totalWidth = (getBoundingRectangle().width / 2 + otherColRectangle.width / 2);
		float overlapX = totalWidth - distX;
//		Log.info("O x" + overlapX);
		if (overlapX < overlapY) {
			// Only do X
			if (getBoundingRectangle().x + getBoundingRectangle().width / 2 < otherColRectangle.x) {
				position.x -= overlapX+1;
				setPosition(position);
				velocity.x *= -0.75; //velocity.mul(0f);
				bulletCollision = bulletCollision + 1;
				Log.info("x left");
			} else {
				position.x += overlapX+1;
				setPosition(position);
				velocity.x *= -0.75;
				bulletCollision = bulletCollision + 1;
				Log.info("x right");
			}
		} else {
			// Only do Y 
			if (getBoundingRectangle().y + getBoundingRectangle().height / 2 < otherColRectangle.y + otherColRectangle.height / 2) {
				Log.info("y below");
				position.y -= overlapY+1;
				setPosition(position);
				bulletCollision = bulletCollision + 1;
				velocity.y *= -0.75;
			} else {
				Log.info("y above");
				position.y += overlapY+1;
				setPosition(position);
				bulletCollision = bulletCollision + 1;
				velocity.y *= -0.75;
			}
		}
		if(bulletCollision > 15){
			destroy();
		}
		
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
