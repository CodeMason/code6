package com.puchisoft.wao;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.minlog.Log;

public class Bullet {
	private TextureRegion texture;

	private Vector2 position;	
	private Vector2 velocity;
	
	public Vector2 maxPosition;
	
	final private float speed = 10.0f;

	private float angle;
	
	public Bullet(TextureRegion texture, Vector2 position, Vector2 velocity, Vector2 direction, Vector2 maxPosition) {
		
		
		this.texture = texture;
		this.position = position;
		this.angle = direction.angle();
		this.velocity = velocity.add(direction.nor().mul(speed)); //todo direction //  * Gdx.graphics.getDeltaTime()
		
	}
	
	private void move(float delta) {
		position.add(velocity.tmp().mul(delta*60));
	}
	
	public void render(SpriteBatch spriteBatch, float delta) {
		this.move(delta);
		
		spriteBatch.draw(texture, position.x, position.y, texture.getRegionWidth()*0.5f, texture.getRegionHeight()*0.5f, texture.getRegionWidth(), texture.getRegionHeight(), 1f, 1f, angle);
	}
}
