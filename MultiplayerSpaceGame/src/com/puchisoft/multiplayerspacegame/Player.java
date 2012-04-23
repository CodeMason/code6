package com.puchisoft.multiplayerspacegame;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.multiplayerspacegame.net.Network.MovementChange;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerShoots;

public class Player {
	private static final int FIRE_DELAY = 500 * 1000000;
	private static final float speedAcc = 10.0f;
	private static final float speedAccTouch = 1.0f;
	private static final float speedRot = 180.0f; // angle degrees
	private static final float speedMax = 50.0f;

	private int id;
	private Sprite sprite;
	private GameMap map;

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
	long mayFireTime = System.nanoTime(); // ms
	public Moon moon;
	
	private Random random = new Random();

	public Player(TextureRegion texture, Vector2 position, Vector2 maxPosition, GameMap map, Color color, Moon moon) {
		this.sprite = new Sprite(texture);
		this.sprite.setColor(color);
		this.position = position;
		this.maxPosition = maxPosition;
		this.map = map;
		this.moon = moon;
	}

	// Returns whether there was a change
	// TODO move into GameMap?
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
		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
			shoot();
		}
		return turning != turningOld || accelerating != acceleratingOld || touchMove;
	}

	private void move(float delta) {

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

	public void shoot() {
		if(mayFireTime > System.nanoTime()){
			return;
		}
		PlayerShoots msgPlayerShoots = new PlayerShoots(id,position.cpy(),velocity.cpy(),direction.cpy());
		map.addBullet(msgPlayerShoots);
		mayFireTime = System.nanoTime() + FIRE_DELAY;
	}
	
	public void hit(){
		velocity.set(0,0);
		direction.rotate(random.nextInt(360));
		position.set(random.nextInt((int)maxPosition.x),random.nextInt((int)maxPosition.y));
	}
	
	public void update(float delta){
		this.move(delta);
	}

	public void render(SpriteBatch spriteBatch) {
		moon.position.lerp(this.position, 0.035f);
		
		moon.render(spriteBatch);		
		
		sprite.draw(spriteBatch);
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

	public int getID() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public Rectangle getBoundingRectangle(){
		return sprite.getBoundingRectangle();
	}
}
