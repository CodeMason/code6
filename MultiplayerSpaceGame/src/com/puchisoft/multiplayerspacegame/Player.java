package com.puchisoft.multiplayerspacegame;

import java.util.Random;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.minlog.Log;
import com.puchisoft.multiplayerspacegame.net.Network.MovementState;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerShoots;
import com.puchisoft.multiplayerspacegame.net.Network.PlayerSpawns;

public class Player {
	
	private static final long FIRE_DELAY =    500 * 1000000L; // nanosec
	private static final long SPAWN_DELAY =  4500 * 1000000L; // nanosec
	private static final float SPEED_ACC = 6.0f;
	private static final int   SPEED_ACC_TURBO = 3; //multiplier
//	private static final float SPEED_ACC_TOUCH = 1.0f;
	private static final float ANGLE_INCR_TOUCH = 22.5f; // Angle increment on touch screens / 22.5f / 45f / 90f
	private static final float SPEED_ROT = 210.0f; // angle degrees per sec
	private static final float SPEED_MAX = 50.0f;
	
	private static final float BOUNDINGBOX_REDUCTION = 0.10f; // percentage
	
	private Rectangle boundingRectangle = new Rectangle();

	private int id;
	private String name;
	private Sprite sprite;
	private GameMap map;
	
	private Vector2 maxPosition;
	private Vector2 position;

	private Vector2 direction = new Vector2(1, 0);
	private Vector2 velocity = new Vector2();

	private Vector2 touchPos;
	
	private float health = 100;

	// input state
	private int turning = 0; // -1, 0, 1
	private int turningOld = turning;
	private int accelerating = 0; // -1, 0, 1
	private int acceleratingOld = turning;
	private float angleLastSent = direction.angle();

	private boolean wasTouched = false;
	private long mayFireTime = 0; // ms
	private long maySpawnTime = 0; // ms
	
	private int score = 0;
	private int lastHitter = -1;
	private boolean isLocal;
	private Random random = new Random();
	private Color colorOrig;
	
	
	public Player(Sprite sprite, Vector2 position, Vector2 maxPosition, GameMap map, Color color, boolean isLocal, float health) {
		this.sprite = new Sprite(sprite);
		this.colorOrig = color;
		this.sprite.setColor(colorOrig);
//		this.sprite.setScale(1.5f);
		this.position = position;
		this.maxPosition = maxPosition;
		this.map = map;
		this.isLocal = isLocal;
		this.health = health;
	}

	// Returns whether there was a change
	// TODO move into GameMap?
	public boolean handleInput(float delta) {
		if(isDead()) return false;

		turningOld = turning;
		acceleratingOld = accelerating;
//		directionOld.x = direction.x; // for new movement
//		directionOld.y = direction.y; // for new movement

		turning = 0;
		accelerating = 0;

		boolean touchMove = false;

		// Android
		// Movement
		if(Gdx.app.getType() == ApplicationType.Android){
			if (Gdx.input.isTouched(0)) {
				if (!wasTouched) { // touchPos == null // just started touching
					touchPos = new Vector2(Gdx.input.getX(0), Gdx.input.getY(0));
					wasTouched = true;
					Log.info("Initial touch saved");
				}
				Vector2 touchDist = touchPos.cpy().sub(Gdx.input.getX(0), Gdx.input.getY(0));
				touchDist.x *= -1;
				// Turn towards goal direction
				// float myDir = direction.angle();
				// float desiredDir = touchDist.angle();
				// // turning = (Math.abs(desiredDir - myDir)) > 180 ? 1 : -1;
				// // 1 = counter, -1 = clockwise
				// // turning = (desiredDir - ((myDir-180)%360)) > 0 ? -1 : 1;
				// if(Math.abs(desiredDir - myDir) >5){
				// float diff = desiredDir - myDir;
				// if(diff < 0){ diff += 360; }
				// turning = diff < 180 ? 1 : -1;
				// Log.info("!!!!! turn debug m " + myDir + " d " + desiredDir +
				// " || " +diff+" | "+turning);
				// }

				if (touchDist.len() < 40) {
					accelerating = 0;
					// Log.info("short drag (turn) " + touchDist.x + " " +
					// touchDist.y + " " + touchDist.len())
//						soundTurn.play();
				} else if (touchDist.len() < 80) {
					accelerating = 1;
//						long soundId = soundAccel.play();
//						soundAccel.setLooping(soundId, false);
//						Gdx.audio.
					// Log.info("medium drag (accel) " + touchDist.x + " " +
					// touchDist.y + " " + touchDist.len());
				} else {
					accelerating = SPEED_ACC_TURBO;
//						soundBoost.play();
					// Log.info("long drag (boost) " + touchDist.x + " " +
					// touchDist.y + " " + touchDist.len());
				}
				
				float touchAngle = Math.round(touchDist.angle() / ANGLE_INCR_TOUCH) * ANGLE_INCR_TOUCH;
				if(touchAngle >= 360.0){ touchAngle = 0;} // otherwise thinks 0.02 differs from 360 greatly
				
				//force packet only if last sent angle differs a bit after snapping to nearest angle increment
				if(Math.abs(direction.angle() - touchAngle) > 1){ // Will always be slightly off
					Log.info("dir change "+direction.angle()+ " "+touchAngle);
					direction.set(1,0).rotate(touchAngle);
//					direction.set(touchDist.cpy()).nor(); // could be optimized
					sprite.setRotation(direction.angle()); // update sprite
					
					touchMove = true;
					angleLastSent = direction.angle();	
				}
				
				
			} else if (wasTouched) {
				wasTouched = false;
				accelerating = 0;
				Log.info("Touch released");
			}
			if (Gdx.input.isTouched(1)) {
				if(mayShoot()) shoot();
			}
		}else{
			// Desktop
			// Movement
			if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) {
				if(accelerating != 1){
					accelerating = 1;
				}
			}else if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) {
				accelerating = -1;
			}
			
			if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) ||Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)){
				accelerating *= SPEED_ACC_TURBO;
			}
			
			if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) {
				turning = 1;
			}else if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) {
				turning = -1;
			}
			

			// Shooting
			if (Gdx.input.isKeyPressed(Keys.SPACE) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
				if(mayShoot()) shoot();
			}
		}
		
		// Sounds
		if(accelerating != acceleratingOld){
			map.gameSounds().stopSound("accel");
			map.gameSounds().stopSound("boost");
			if(accelerating == 0){
			}else if(Math.abs(accelerating) == 1){
				map.gameSounds().playAndLoopSound("accel");
			}else if(Math.abs(accelerating) == SPEED_ACC_TURBO){
				map.gameSounds().playAndLoopSound("boost");
			}
		}
		
		return turning != turningOld || accelerating != acceleratingOld || touchMove;
	}

	private void move(float delta) {

		direction.rotate(turning * delta * SPEED_ROT);
		sprite.setRotation(direction.angle()); // update sprite

		velocity.add(direction.tmp().mul(SPEED_ACC * delta * accelerating));

		if (velocity.len() > SPEED_MAX) {
			velocity.nor().mul(SPEED_MAX);
		}

		getPosition().add(velocity.tmp().mul(delta * 60));

		//TODO move into central collision detection logic
		// Bounce
		if (position.x < 0 || position.x > maxPosition.x - sprite.getBoundingRectangle().getWidth()) {
			velocity.x *= -0.3;
		} else if (position.y < 0 || position.y > maxPosition.y - sprite.getBoundingRectangle().getHeight()) {
			velocity.y *= -0.3;
		}
		// Prevent escape
		getPosition().x = Math.max(0, Math.min(maxPosition.x - sprite.getBoundingRectangle().getWidth(), getPosition().x));
		getPosition().y = Math.max(0, Math.min(maxPosition.y - sprite.getBoundingRectangle().getHeight(), getPosition().y));
		
		sprite.setPosition(getPosition().x, getPosition().y); // update sprite
		
	}
	
	public boolean mayShoot(){
		return System.nanoTime() > mayFireTime;
	}

	public boolean shoot() {
//		if(!mayShoot()){ // Commented out to turn off spam cheat detection
//			return false;
//		}
		PlayerShoots msgPlayerShoots = new PlayerShoots(id,getPosition().cpy(),velocity.cpy(),direction.cpy());
		map.addBullet(msgPlayerShoots);
		mayFireTime = System.nanoTime() + FIRE_DELAY;
		
		if(isLocal){
			map.gameSounds().play("shoot");
			map.clientSendMessage(msgPlayerShoots);
		}
		return true;
	}
	
	// returns if Player was killed by hit
	public boolean hit(float damage, int hitterID){
		if(isDead()) return false; //only called from server messages
		
		lastHitter = hitterID;
		
		health -= damage;
		if(health <= 0){ // On Death, stop movement, set respawn time
			velocity.set(0,0);
			maySpawnTime = System.nanoTime() + SPAWN_DELAY;
			score--;
			return true;
		}
		return false;
	}
	
	public boolean isDead(){
		return health <= 0;
	}
	
	/*
	 * Called only by server to spawn player, if he may spawn
	 */
	public boolean spawnIfAppropriate(){
		if(isDead() && System.nanoTime() > maySpawnTime){
			spawn();
			return true;
		}
		return false;
	}
	/*
	 * Called only by server to spawn player (call directly for new rounds)
	 */
	public void spawn(){
		direction.rotate(random .nextInt(360));
		position.set(random.nextInt((int)maxPosition.x),random.nextInt((int)maxPosition.y));
		velocity.set(0,0);
		health = 100;
		
		PlayerSpawns msg = new PlayerSpawns(id,getMovementState());
		map.onPlayerSpawn(msg);
		map.serverSendMessage(msg);
	}
	
	// Client
	public void spawn(PlayerSpawns msg){
		health = 100;
		setMovementState(msg.movementState);
	}
	
	public void update(float delta){
		if(isDead()){
			if(sprite.getScaleX() > 0.3f){
				sprite.setColor(new Color(random.nextFloat(),random.nextFloat(),random.nextFloat(),1));
				sprite.setScale(sprite.getScaleX() - 0.3f * delta);
			}
		}else{
			// TODO this sets dirty flag - only do once onSpawn
			sprite.setColor(colorOrig);
			sprite.setScale(1);
			this.move(delta);
		}
	}

	public void render(SpriteBatch spriteBatch) {
//		if(isDead()) return;
		
		sprite.draw(spriteBatch);
	}
	public void renderNameTag(SpriteBatch spriteBatch, BitmapFont fontNameTag) {
		if(isDead()) return;
		
		fontNameTag.setColor(sprite.getColor());
		fontNameTag.draw(spriteBatch, name+" ("+(int)health+"%)["+score+"]", getPosition().x-16, getPosition().y + fontNameTag.getLineHeight()+48);
	}

	public Vector3 getDesiredCameraPosition(Vector3 camPos, float delta) {
//		Vector2 offset = velocity.cpy().mul(400 * delta);
//
//		// Clamp cam position to always show our player
//		offset.x = Math.max(-1 * Gdx.graphics.getWidth() * 0.3f, Math.min(Gdx.graphics.getWidth() * 0.3f, offset.x));
//		offset.y = Math.max(-1 * Gdx.graphics.getHeight() * 0.3f, Math.min(Gdx.graphics.getHeight() * 0.3f, offset.y));
//
//		Vector2 dest = getPosition().cpy().add(offset);
//		camPos.lerp(new Vector3(dest.x, dest.y, 0), 30f * delta);

		return new Vector3(position.x, position.y, 0); // camPos
	}

	public MovementState getMovementState() {
		return new MovementState(id, turning, accelerating, getPosition(), direction, velocity);
	}

	public void setMovementState(MovementState msg) {
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
	
	public Rectangle getBoundingRectangle() {
		float scaleX = sprite.getBoundingRectangle().width * BOUNDINGBOX_REDUCTION;
		float scaleY = sprite.getBoundingRectangle().height * BOUNDINGBOX_REDUCTION;
		boundingRectangle.x = sprite.getBoundingRectangle().x+scaleX;
		boundingRectangle.y = sprite.getBoundingRectangle().y+scaleY;
		boundingRectangle.width = sprite.getBoundingRectangle().width-(scaleX * 2);
		boundingRectangle.height = sprite.getBoundingRectangle().height-(scaleY * 2);
		
		return boundingRectangle;
	}

	public void preventOverlap(Rectangle otherColRectangle, float delta) {
		
		float distY = Math.abs((getBoundingRectangle().y + getBoundingRectangle().height / 2) - (otherColRectangle.y + otherColRectangle.height / 2));
		float totalHeight = (getBoundingRectangle().height / 2 + otherColRectangle.height / 2);
		float overlapY = totalHeight - distY;

		float distX = Math.abs((getBoundingRectangle().x + getBoundingRectangle().width / 2) - (otherColRectangle.x + otherColRectangle.width / 2));
		float totalWidth = (getBoundingRectangle().width / 2 + otherColRectangle.width / 2);
		float overlapX = totalWidth - distX;
		if (overlapX < overlapY) {
			// Only do X
			if (getBoundingRectangle().x + getBoundingRectangle().width / 2 < otherColRectangle.x) {
				position.x -= overlapX+1;
				setPosition(position);
				velocity.x *= -0.3; //velocity.mul(0f);
//				Log.info("x left");
			} else {
				position.x += overlapX+1;
				setPosition(position);
				velocity.x *= -0.3;
//				Log.info("x right");
			}
		} else {
			// Only do Y 
			if (getBoundingRectangle().y + getBoundingRectangle().height / 2 < otherColRectangle.y + otherColRectangle.height / 2) {
//				Log.info("y below");
				position.y -= overlapY+1;
				setPosition(position);
				velocity.y *= -0.3;
			} else {
//				Log.info("y above");
				position.y += overlapY+1;
				setPosition(position);
				velocity.y *= -0.3;
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vector2 getPosition() {
		return position;
	}

	public void setPosition(Vector2 position) {
		this.position.set(position.x,position.y);
		this.sprite.setPosition(position.x,position.y); // update sprite
	}
	
	public void setDirection(Vector2 direction){
		this.direction.set(direction.x,direction.y).nor();
		this.sprite.setRotation(direction.angle()); // update sprite
	}
	
	public void setVelocity(Vector2 velocity) {
		this.velocity.set(velocity.x,velocity.y);
	}

	public void addScore(int amount) {
		score  += amount;
	}
	
	public void setScore(int amount) {
		score  = amount;
	}

	public int getScore() {
		return score;
	}
	
	public float getHealth() {
		return health;
	}
	
	public void setHealth(float health) {
		this.health = health;
	}

	public int getLastHitter() {
		return lastHitter;
	}

	public Color getColor() {
		return this.sprite.getColor();
	}
}
