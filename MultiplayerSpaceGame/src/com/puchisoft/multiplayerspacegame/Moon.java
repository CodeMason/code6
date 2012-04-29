package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.minlog.Log;

public class Moon {
	private static final float speedAcc = 10.0f;
	private static final float speedAccTouch = 1.0f;
	private static final float speedRot = 180.0f; // angle degrees
	private static final float speedMax = 60.0f;
	
	private Sprite sprite;
	
	public Vector2 maxPosition;
	public Vector2 position;
	
	private Vector2 direction = new Vector2(1, 0);
	public Vector2 velocity = new Vector2();
	
	private Vector2 touchPos;
	
	// input state
	private int turning = 0; // -1, 0, 1
	private int turningOld = turning;
	private int accelerating = 0; // -1, 0, 1
	private int acceleratingOld = turning;

	boolean wasTouched = false;
	
	public Moon(Texture texture, Vector2 position, Vector2 maxPosition) {
		this.sprite = new Sprite(texture);
		this.position = position;
		this.maxPosition = maxPosition;
	}
	
	public boolean handleInput(float delta) {

		turningOld = turning;
		acceleratingOld = accelerating;

		turning = 0;
		accelerating = 0;

		boolean touchMove = false;

		// Android
		// Movement
		if (Gdx.input.isTouched()) {
			if (!wasTouched) { // touchPos == null // just started touching
				touchPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
			}
			wasTouched = true;
		} else if (wasTouched) { // just stopped touching
			touchPos.sub(Gdx.input.getX(), Gdx.input.getY());
			if (touchPos.len() > 10) { // deadzone ... to short to be counted
										// as a drag
				touchPos.x *= -1;
				Log.info("drag " + touchPos.x + " " + touchPos.y + " " + touchPos.len2());
				direction.set(touchPos.tmp()).nor();
				velocity.add(touchPos.mul(speedAccTouch * delta));
			} else {
				//shoot();
				Log.info("touch");
			}
			wasTouched = false;
			touchMove = true;
		}

		// Desktop
		// Movement
		if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.S)
				|| Gdx.input.isKeyPressed(Keys.D)) {

			if (Gdx.input.isKeyPressed(Keys.W)) {
				accelerating = 1;
			} else if (Gdx.input.isKeyPressed(Keys.S)) {
				accelerating = -1;
			}
			if (Gdx.input.isKeyPressed(Keys.A)) {
				turning = 1;
			} else if (Gdx.input.isKeyPressed(Keys.D)) {
				turning = -1;
			}
		}

		
		return turning != turningOld || accelerating != acceleratingOld || touchMove;
	}

	public void move(float delta) {

		direction.rotate(turning * delta * speedRot);
		sprite.setRotation(direction.angle()); // update sprite

		velocity.add(direction.tmp().mul(speedAcc * delta * accelerating));

		if (velocity.len() > speedMax) {
			velocity.nor().mul(speedMax);
		}

		position.add(velocity.tmp().mul(delta * 60));

		// Bounce
		if (position.x < 0 || position.x > maxPosition.x - sprite.getWidth()) {
			velocity.x *= -0.3;
		} else if (position.y < 0 || position.y > maxPosition.y - sprite.getHeight()) {
			velocity.y *= -0.3;
		}
		// Prevent escape
		position.x = Math.max(0, Math.min(maxPosition.x - sprite.getWidth(), position.x));
		position.y = Math.max(0, Math.min(maxPosition.y - sprite.getHeight(), position.y));
		
		sprite.setPosition(position.x, position.y); // update sprite
		
	}


	public void render(SpriteBatch spriteBatch) {
		// TODO Auto-generated method stub
//		spriteBatch.draw(texture, position.x, position.y, 0, 0, 
//				texture.getWidth(), texture.getHeight());
		sprite.draw(spriteBatch);
	}
	
}
