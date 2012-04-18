package com.puchisoft.wao;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.wao.net.Network.MovementChange;

public class Player {
	private TextureRegion texture;
	private int id;

	public Vector2 maxPosition;
	public Vector2 position;
	
	private Vector2 direction = new Vector2(1,0);
	private Vector2 velocity = new Vector2();
	
	// input state
	private int turning = 0; // -1, 0, 1
	private int turningOld = turning;
	private int accelerating = 0; // -1, 0, 1
	private int acceleratingOld = turning;
	
	private float speedAcc = 0.5f;
	private float speedRot = 5f; //angle degrees
	
	public Player(TextureRegion texture, Vector2 position, Vector2 maxPosition) {
		this.texture = texture;
		this.position = position;
		this.maxPosition = maxPosition;
	}

	// Returns whether there was a change
	public boolean handleInput() {
		
		turningOld = turning;
		acceleratingOld = accelerating;
		
		turning = 0;
		accelerating = 0;
		if (Gdx.input.isTouched() || Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.A)
				|| Gdx.input.isKeyPressed(Keys.S)
				|| Gdx.input.isKeyPressed(Keys.D)) {
			
			if(Gdx.input.isTouched()){
				Log.info(Gdx.input.getX()+" "+Gdx.input.getY());
//				direction.x = Gdx.input.getX() > Gdx.graphics.getWidth()/2 ? 1 : -1;
//				direction.y = Gdx.input.getY() > Gdx.graphics.getHeight()/2 ? -1 : 1;
			}
			
			if (Gdx.input.isKeyPressed(Keys.W)) {
				accelerating = 1;
			}
			if (Gdx.input.isKeyPressed(Keys.S)) {
				accelerating = -1;
			}
			if (Gdx.input.isKeyPressed(Keys.A)) {
				turning = 1;
			}
			if (Gdx.input.isKeyPressed(Keys.D)) {
				turning = -1;
			}
		}
		return turning != turningOld || accelerating != acceleratingOld;
	}

	private void move() {
		
		direction.rotate(turning * speedRot);
		
		velocity.add(direction.cpy().mul(speedAcc * accelerating));
		
		position.add(velocity);

		// Prevent escape
		position.x = Math.max(0,
				Math.min(maxPosition.x - texture.getRegionWidth(), position.x));
		position.y = Math
				.max(0, Math.min(maxPosition.y - texture.getRegionHeight(),position.y));
	}

	public void render(SpriteBatch spriteBatch) {
		this.move();

		spriteBatch.draw(texture, position.x, position.y, texture.getRegionWidth()/2, texture.getRegionHeight()/2, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, direction.angle()); 
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public MovementChange getMovementState(){
		return new MovementChange(id,turning, accelerating, position, direction, velocity);
	}

	public void setMovementState(MovementChange msg) {
		this.turning = msg.turning;
		this.accelerating = msg.accelerating;
		this.position = msg.position;
		this.direction = msg.direction;
		this.velocity = msg.velocity;
	}
}
