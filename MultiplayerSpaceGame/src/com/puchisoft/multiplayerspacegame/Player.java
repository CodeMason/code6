package com.puchisoft.multiplayerspacegame;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
	private static final long FIRE_DELAY =    500 * 1000000L; // nanosec
	private static final long SPAWN_DELAY =  2500 * 1000000L; // nanosec
	private static final float SPEED_ACC = 6.0f;
	private static final int   SPEED_ACC_TURBO = 3; //multiplier
//	private static final float SPEED_ACC_TOUCH = 1.0f;
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
//	private Vector2 directionOld = direction;

	private boolean wasTouched = false;
	private long mayFireTime = 0; // ms
	private long maySpawnTime = 0; // ms
	
	private int score = 0;
	private int lastHitter = -1;
	private boolean isLocal;
	private Random random = new Random();
	private Color colorOrig;


	public Player(TextureRegion texture, Vector2 position, Vector2 maxPosition, GameMap map, Color color, boolean isLocal) {
		this.sprite = new Sprite(texture);
		this.colorOrig = color;
		this.sprite.setColor(colorOrig);
//		this.sprite.setScale(1.5f);
		this.setPosition(position);
		this.maxPosition = maxPosition;
		this.map = map;
		this.isLocal = isLocal;
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
//		if(Gdx.app.getType() == ApplicationType.Android){
			if (Gdx.input.isTouched(0)) {
				if (!wasTouched) { // touchPos == null // just started touching
					touchPos = new Vector2(Gdx.input.getX(0), Gdx.input.getY(0));
					wasTouched = true;
					Log.info("Initial touch saved");
				}
				if (wasTouched) { // still touching
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
					direction.set(touchDist.cpy()).nor(); // could be optimized
	
					if (touchDist.len() < 40) {
						accelerating = 0;
						// Log.info("short drag (turn) " + touchDist.x + " " +
						// touchDist.y + " " + touchDist.len());
					} else if (touchDist.len() < 80) {
						accelerating = 1;
						// Log.info("medium drag (accel) " + touchDist.x + " " +
						// touchDist.y + " " + touchDist.len());
					} else {
						accelerating = SPEED_ACC_TURBO;
						// Log.info("long drag (boost) " + touchDist.x + " " +
						// touchDist.y + " " + touchDist.len());
					}
				}
				touchMove = true;
			} else if (wasTouched) {
				wasTouched = false;
				accelerating = 0;
				Log.info("Touch released");
			}
			if (Gdx.input.isTouched(1)) {
				shoot();
			}
//		}else{
			// Desktop
			// Movement
			if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP)) {
				accelerating = 1;
			}else if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN)) {
				accelerating = -1;
			}
			
			if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) {
				turning = 1;
			}else if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) {
				turning = -1;
			}		
			
			if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) ||Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)){
				accelerating *= SPEED_ACC_TURBO;
			}
			
			// Shooting
			if (Gdx.input.isKeyPressed(Keys.SPACE) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
				shoot();
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

	public void shoot() {
		if(mayFireTime > System.nanoTime()){
			return;
		}
		PlayerShoots msgPlayerShoots = new PlayerShoots(id,getPosition().cpy(),velocity.cpy(),direction.cpy());
		map.addBullet(msgPlayerShoots);
		mayFireTime = System.nanoTime() + FIRE_DELAY;
	}
	
	public void hit(float damage, int hitterID){
		if(isDead() && !isLocal) return; // other players' health only updated from server messages
		
		lastHitter = hitterID;
		
		health -= damage;
		if(health <= 0 && isLocal){ // only local player trigger self-death (otherwise wait on server)
			velocity.set(0,0);
			maySpawnTime = System.nanoTime() + SPAWN_DELAY;
		}
	}
	
	public boolean isDead(){
		return health <= 0;
	}
	
	public void update(float delta){
		if(isDead()){
			sprite.setColor(new Color(random.nextFloat(),random.nextFloat(),random.nextFloat(),1));
			sprite.setScale(sprite.getScaleX() - 0.3f * delta);
			
			// Locally respawn and tell others
			if(System.nanoTime() > maySpawnTime && isLocal){
				direction.rotate(random .nextInt(360));
				getPosition().set(random.nextInt((int)maxPosition.x),random.nextInt((int)maxPosition.y));
				health = 100;
				map.sendMessage(getMovementState());
			}
		}else{
			// TODO this sets dirty flag - only do once when needed
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
		Vector2 offset = velocity.cpy().mul(400 * delta);

		// Clamp cam position to always show our player
		offset.x = Math.max(-1 * Gdx.graphics.getWidth() * 0.3f, Math.min(Gdx.graphics.getWidth() * 0.3f, offset.x));
		offset.y = Math.max(-1 * Gdx.graphics.getHeight() * 0.3f, Math.min(Gdx.graphics.getHeight() * 0.3f, offset.y));

		Vector2 dest = getPosition().cpy().add(offset);
		camPos.lerp(new Vector3(dest.x, dest.y, 0), 30f * delta);

		return camPos;
	}

	public MovementChange getMovementState() {
		return new MovementChange(id, turning, accelerating, getPosition(), direction, velocity, health);
	}

	public void setMovementState(MovementChange msg) {
		this.turning = msg.turning;
		this.accelerating = msg.accelerating;
		this.position = msg.position;
		this.direction = msg.direction;
		this.velocity = msg.velocity;
		this.health = msg.health;
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
		this.position = position;
		sprite.setPosition(getPosition().x, getPosition().y); // update sprite
	}

	public void addScore(int amount) {
		score  += amount;
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
}
