package com.puchisoft.wao;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.wao.net.Network.MovementChange;

public class Player {
	private Texture texture;
	private int id;

	public Vector2 maxPosition;
	public Vector2 position;
	private Vector2 direction = new Vector2();
	private Vector2 oldDirection = new Vector2();
	
	private float speed = 5;
	
	private boolean isMoving = false;

	public Player(Texture texture, Vector2 position, Vector2 maxPosition) {
		this.texture = texture;
		this.position = position;
		this.maxPosition = maxPosition;
	}

	// Returns whether there was a change
	public boolean handleInput() {
		
		boolean wasMoving = isMoving; 
		oldDirection.set(direction);
		
		if (Gdx.input.isTouched() || Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.A)
				|| Gdx.input.isKeyPressed(Keys.S)
				|| Gdx.input.isKeyPressed(Keys.D)) {
			
			direction.set(0,0);
			if(Gdx.input.isTouched()){
				Log.info(Gdx.input.getX()+" "+Gdx.input.getY());
				direction.x = Gdx.input.getX() > Gdx.graphics.getWidth()/2 ? 1 : -1;
				direction.y = Gdx.input.getY() > Gdx.graphics.getHeight()/2 ? -1 : 1;
			}
			
			if (Gdx.input.isKeyPressed(Keys.W)) {
				direction.y = 1;
			}
			if (Gdx.input.isKeyPressed(Keys.S)) {
				direction.y = -1;
			}
			if (Gdx.input.isKeyPressed(Keys.A)) {
				direction.x = -1;
			}
			if (Gdx.input.isKeyPressed(Keys.D)) {
				direction.x = 1;
			}
//			
			direction.nor().mul(speed);
			isMoving = true;
			
		}
		else{
			isMoving = false;
		}
		return wasMoving != isMoving || !oldDirection.equals(direction);
	}
		
	private void move(){
		if(isMoving){
			position.add(direction);
			
			// Prevent escape
			position.x = Math.max(0, Math.min(
					maxPosition.x - texture.getWidth(), position.x));
			position.y = Math.max(0, Math.min(
					maxPosition.y - texture.getHeight(), position.y));
		}
	}

	public void render(SpriteBatch spriteBatch) {
		this.move();

		spriteBatch.draw(texture, position.x, position.y, 0, 0,
				texture.getWidth(), texture.getHeight());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public MovementChange getMovementState(){
		return new MovementChange(id,isMoving,position,direction);
	}

	public void setMovementState(MovementChange msg) {
		this.direction = msg.direction;
		this.isMoving = msg.isMoving;
		this.position = msg.position;
	}
}
