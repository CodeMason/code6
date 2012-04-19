package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.multiplayerspacegame.net.Network.MovementChange;

public class Player {
	private static final int FIRE_DELAY = 500;
	private static final float speedAcc = 20.0f;
	private static final float speedAccTouch = 1.0f;
	private static final float speedRot = 180.0f; // angle degrees
	private static final float speedMax = 50.0f;

	private TextureRegion texture;
	private int id;
	private GameMap map;

	public Vector2 maxPosition;
	public Vector2 position;

	private Vector2 direction = new Vector2(1, 0);
	private Vector2 velocity = new Vector2();

	private Vector2 touchPos;

	// input state
	private int turning = 0; // -1, 0, 1
	private int turningOld = turning;
	private int accelerating = 0; // -1, 0, 1
	private int acceleratingOld = turning;

	boolean wasTouched = false;
	long mayFireTime = System.nanoTime() / 1000000; // ms

	public Player(TextureRegion texture, Vector2 position, Vector2 maxPosition, GameMap map) {
		this.texture = texture;
		this.position = position;
		this.maxPosition = maxPosition;
		this.map = map;
	}

	// Returns whether there was a change
	public boolean handleInput() {

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
			if (touchPos.len2() > 50) { // deadzone ... to short to be counted
										// as a drag
				touchPos.x *= -1;
				Log.info("drag " + touchPos.x + " " + touchPos.y + " " + touchPos.len2());
				direction.set(touchPos.tmp()).nor();
				velocity.add(touchPos.mul(speedAccTouch * Gdx.graphics.getDeltaTime()));
			} else {
				shoot();
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
		// Shooting
		if (Gdx.input.isKeyPressed(Keys.SPACE) && mayFireTime < System.nanoTime() / 1000000) {
			shoot();
		}
		return turning != turningOld || accelerating != acceleratingOld || touchMove;
	}

	private void move(float delta) {

		direction.rotate(turning * delta * speedRot);

		velocity.add(direction.tmp().mul(speedAcc * delta * accelerating));

		if (velocity.len() > speedMax) {
			velocity.nor().mul(speedMax);
		}

		position.add(velocity.tmp().mul(delta * 60));

		// Prevent escape
		if (position.x < 0 || position.x > maxPosition.x - texture.getRegionWidth()) {
			velocity.x *= -0.3;
		} else if (position.y < 0 || position.y > maxPosition.y - texture.getRegionHeight()) {
			velocity.y *= -0.3;
		}

		position.x = Math.max(0, Math.min(maxPosition.x - texture.getRegionWidth(), position.x));
		position.y = Math.max(0, Math.min(maxPosition.y - texture.getRegionHeight(), position.y));
	}

	public void shoot() {
		map.addBullet(this, position.cpy(), velocity.cpy(), direction.cpy());
		mayFireTime = (System.nanoTime() / 1000000) + FIRE_DELAY;
	}

	public void render(SpriteBatch spriteBatch, float delta) {
		this.move(delta);

		spriteBatch.draw(texture, position.x, position.y, texture.getRegionWidth() * 0.5f, texture.getRegionHeight() * 0.5f,
				texture.getRegionWidth(), texture.getRegionHeight(), 1, 1, direction.angle());
	}

	public Vector3 getDesiredCameraPosition(Vector3 camPos, float delta) {
		Vector2 offset = velocity.cpy().mul(400 * delta);

		// Clamp cam position to always show our player
		offset.x = Math.max(-1 * Gdx.graphics.getWidth() * 0.3f, Math.min(Gdx.graphics.getWidth() * 0.3f, offset.x));
		offset.y = Math.max(-1 * Gdx.graphics.getHeight() * 0.3f, Math.min(Gdx.graphics.getHeight() * 0.3f, offset.y));

		Vector2 dest = position.cpy().add(offset);
		camPos.lerp(new Vector3(dest.x, dest.y, 0), 30f * delta);

		return camPos;
	}

	public MovementChange getMovementState() {
		return new MovementChange(id, turning, accelerating, position, direction, velocity);
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
