package com.puchisoft.wao;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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
	
	final private float speedAcc = 30.0f;
	final private float speedRot = 180.0f; //angle degrees
	final private float speedMax = 80.0f;
	
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
				turning = Gdx.input.getX() > Gdx.graphics.getWidth()/2 ? 1 : -1;
				accelerating = Gdx.input.getY() > Gdx.graphics.getHeight()/2 ? -1 : 1;
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

	private void move(float delta) {
		
		direction.rotate(turning * delta * speedRot);
		
		velocity.add(direction.tmp().mul(speedAcc * delta * accelerating));
		
		if(velocity.len() > speedMax) {
			velocity.nor().mul(speedMax);
		}
		
		position.add(velocity.tmp().mul(delta*60));

		// Prevent escape
		if(position.x < 0 || position.x > maxPosition.x - texture.getRegionWidth()){
			velocity.x *= -0.3;
		}	
		else if(position.y < 0 || position.y > maxPosition.y - texture.getRegionHeight()){
			velocity.y *= -0.3;
		}
		
		position.x = Math.max(0,
				Math.min(maxPosition.x - texture.getRegionWidth(), position.x));
		position.y = Math
				.max(0, Math.min(maxPosition.y - texture.getRegionHeight(),position.y));
	}

	public void render(SpriteBatch spriteBatch, float delta) {
		this.move(delta);

		spriteBatch.draw(texture, position.x, position.y, texture.getRegionWidth()*0.5f, texture.getRegionHeight()*0.5f, texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, direction.angle()); 
	}
	
	public Vector3 getDesiredCameraPosition(Vector3 camPos, float delta){
		Vector2 offset = velocity.cpy().mul(400*delta);

		// Clamp cam position to always show our player
		offset.x = Math.max(-1*Gdx.graphics.getWidth()*0.3f , Math.min(Gdx.graphics.getWidth()*0.3f, offset.x));
		offset.y = Math.max(-1*Gdx.graphics.getHeight()*0.3f ,Math.min(Gdx.graphics.getHeight()*0.3f, offset.y));
		
		Vector2 dest = position.cpy().add(offset);
		camPos.lerp(new Vector3(dest.x,dest.y,0), 30f* delta);
		
		return camPos;
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
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
}
