package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.multiplayerspacegame.net.Network.MovementChange;

public class Moon {
	private static final float speedAcc = 10.0f;
	private static final float speedAccTouch = 1.0f;
	private static final float speedRot = 180.0f; // angle degrees
	private static final float speedMax = 60.0f;
	
	private int id;
	private Sprite sprite;
	
	public Vector2 maxPosition;
	private Vector2 position;
	
	private Vector2 direction = new Vector2(1, 0);


	public Vector2 velocity = new Vector2();
	
	private Vector2 touchPos;
	
	// input state
	private int turning = 0; // -1, 0, 1
	private int turningOld = turning;
	private int accelerating = 0; // -1, 0, 1
	private int acceleratingOld = turning;

	boolean wasTouched = false;
	private float health;
	
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
//		if (Gdx.input.isTouched()) {
//			if (!wasTouched) { // touchPos == null // just started touching
//				touchPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
//			}
//			wasTouched = true;
//		} else if (wasTouched) { // just stopped touching
//			touchPos.sub(Gdx.input.getX(), Gdx.input.getY());
//			if (touchPos.len() > 10) { // deadzone ... to short to be counted
//										// as a drag
//				touchPos.x *= -1;
//				Log.info("drag " + touchPos.x + " " + touchPos.y + " " + touchPos.len2());
//				direction.set(touchPos.tmp()).nor();
//				velocity.add(touchPos.mul(speedAccTouch * delta));
//			} else {
//				//shoot();
//				Log.info("touch");
//			}
//			wasTouched = false;
//			touchMove = true;
//		}

		// Desktop
		// Movement
		if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.S)
				|| Gdx.input.isKeyPressed(Keys.D)) {

//			if (Gdx.input.isKeyPressed(Keys.W)) {
//				accelerating = 1;
//			} else if (Gdx.input.isKeyPressed(Keys.S)) {
//				accelerating = -1;
//			}
			if (Gdx.input.isKeyPressed(Keys.A)) {
				turning = 1;
			} else if (Gdx.input.isKeyPressed(Keys.D)) {
				turning = -1;
			}
		}

		
		return turning != turningOld || accelerating != acceleratingOld || touchMove;
	}

	public void move(float delta) {

		Vector2 newDirection = direction.cpy().rotate(turning * delta * speedRot);
		direction.lerp(newDirection, 0.05f);
		sprite.setRotation(direction.angle()); // update sprite

//		velocity.add(direction.tmp().mul(speedAcc * delta * accelerating));
//
//		if (velocity.len() > speedMax) {
//			velocity.nor().mul(speedMax);
//		}
//
//		position.add(velocity.tmp().mul(delta * 60));

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
	
	public Rectangle getBoundingRectangle(){
		return sprite.getBoundingRectangle();
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
				velocity.x *= -0.3; //velocity.mul(0f);
				Log.info("x left");
			} else {
				position.x += overlapX+1;
				setPosition(position);
				velocity.x *= -0.3;
				Log.info("x right");
			}
		} else {
			// Only do Y 
			if (getBoundingRectangle().y + getBoundingRectangle().height / 2 < otherColRectangle.y + otherColRectangle.height / 2) {
				Log.info("y below");
				position.y -= overlapY+1;
				setPosition(position);
				velocity.y *= -0.3;
			} else {
				Log.info("y above");
				position.y += overlapY+1;
				setPosition(position);
				velocity.y *= -0.3;
			}
		}
	}
	
	public Vector3 getDesiredCameraPosition(Vector3 camPos, float delta) {
		Vector2 offset = velocity.cpy().mul(400 * delta);

		// Clamp cam position to always show our player
		offset.x = Math.max(-1 * Gdx.graphics.getWidth() * 0.3f, Math.min(Gdx.graphics.getWidth() * 0.3f, offset.x));
		offset.y = Math.max(-1 * Gdx.graphics.getHeight() * 0.3f, Math.min(Gdx.graphics.getHeight() * 0.3f, offset.y));

		Vector2 dest = getPosition().cpy().add(offset);
		camPos.lerp(new Vector3(dest.x, dest.y, 0), 30f * delta);

		return camPos;
	}
	
	public MovementChange getMovementState() {
		return new MovementChange(id, turning, accelerating, position, direction, velocity, health);
	}

	public void setMovementState(MovementChange msg) {
		turning = msg.turning;
		accelerating = msg.accelerating;
		position = msg.position;
		direction = msg.direction;
		velocity = msg.velocity;
		health = msg.health;
	}
	
	public int getID() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Vector2 getPosition() {
		return position;
	}
	
	public void setPosition(Vector2 position) {
		this.position = position;
		sprite.setPosition(getPosition().x, getPosition().y); // update sprite
	}
	public Vector2 getDirection() {
		return direction;
	}

	public void setDirection(Vector2 direction) {
		this.direction = direction;
	}
	
}
