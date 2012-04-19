package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
	private TextureRegion texture;

	private int playerID;
	
	private Vector2 position;	
	private Vector2 velocity;
	
	public Vector2 maxPosition;
	
	final private float speed = 15.0f;

	private float angle;
	
	public boolean destroyed = false;

	
	public Bullet(TextureRegion texture, int playerID, Vector2 position, Vector2 baseVelocity, Vector2 direction, Vector2 maxPosition) {
		
		this.texture = texture;
		this.playerID = playerID;
		this.position = position;
		this.angle = direction.angle();
		this.velocity = baseVelocity.add(direction.nor().mul(speed));
		this.maxPosition = maxPosition;
		
	}
	
	private void move(float delta) {
		position.add(velocity.tmp().mul(delta*60));
		
		
	}
	
	private boolean collision(){
		if(position.x < 0 || position.x > maxPosition.x - texture.getRegionWidth()){
			return destroy();
		}	
		else if(position.y < 0 || position.y > maxPosition.y - texture.getRegionHeight()){
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
		spriteBatch.draw(texture, position.x, position.y, texture.getRegionWidth()*0.5f, texture.getRegionHeight()*0.5f, texture.getRegionWidth(), texture.getRegionHeight(), 1f, 1f, angle);
	}

	public int getPlayerID() {
		return playerID;
	}
}
