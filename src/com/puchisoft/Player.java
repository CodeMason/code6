package com.puchisoft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.puchisoft.net.Network.MovementChange;

public class Player {
	private Texture texture;
	private int id;

	private Vector2 position = new Vector2(50, 50);
	private double angle = 0;
	private double speed = 5;
	private boolean isMoving = false;

	public Player(Texture texture, Vector2 position) {
		this.texture = texture;
		this.position = position;
	}

	// Returns whether there was a change
	public boolean handleInput() {
		
		boolean wasMoving = isMoving; 
		double oldAngle = angle;
		if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.A)
				|| Gdx.input.isKeyPressed(Keys.S)
				|| Gdx.input.isKeyPressed(Keys.D)) {
			
			
			if (Gdx.input.isKeyPressed(Keys.W)) {
				if (Gdx.input.isKeyPressed(Keys.A)) {
					angle = (3.0 / 4.0) * Math.PI;
				} else if (Gdx.input.isKeyPressed(Keys.D)) {
					angle = (1.0 / 4.0) * Math.PI;
				} else {
					angle = Math.PI / 2;
				}
			} else if (Gdx.input.isKeyPressed(Keys.S)) {
				if (Gdx.input.isKeyPressed(Keys.A)) {
					angle = (5.0 / 4.0) * Math.PI;
				} else if (Gdx.input.isKeyPressed(Keys.D)) {
					angle = (7.0 / 4.0) * Math.PI;
				} else {
					angle = (3.0 / 2.0) * Math.PI;
				}
			} else if (Gdx.input.isKeyPressed(Keys.A)) {
				angle = Math.PI;
			} else if (Gdx.input.isKeyPressed(Keys.D)) {
				angle = 0;
			}
			isMoving = true;
			
		}
		else{
			isMoving = false;
		}
		return wasMoving != isMoving || oldAngle != angle;
	}
		
	private void move(){
		if(isMoving){
			position.y += Math.sin(angle) * speed;
			position.x += Math.cos(angle) * speed;
			
			// Prevent escape
			position.x = Math.max(0, Math.min(
					Gdx.graphics.getWidth() - texture.getWidth(), position.x));
			position.y = Math.max(0, Math.min(
					Gdx.graphics.getHeight() - texture.getHeight(), position.y));
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
		return new MovementChange(id,angle,isMoving,position);
	}

	public void setMovementState(MovementChange msg) {
		this.angle = msg.angle;
		this.isMoving = msg.isMoving;
		this.position = msg.position;
	}
}
