package com.puchisoft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Player {
	Texture texture;
	
	Vector2 position = new Vector2(50, 50);
	
	public Player(Texture texture, Vector2 position){
		this.texture = texture;
		this.position = position;
	}

	public void render(SpriteBatch spriteBatch) {
		if (Gdx.input.isKeyPressed(Keys.W)) {
			position.y += 3;
		}
		else if(Gdx.input.isKeyPressed(Keys.S)) {
			position.y -= 3;
		}
		if (Gdx.input.isKeyPressed(Keys.A)) {
			position.x -= 3;
		}
		else if(Gdx.input.isKeyPressed(Keys.D)) {
			position.x += 3;
		}
		
		spriteBatch.draw(texture, position.x, position.y, 0, 0, texture.getWidth(), texture.getHeight());
	}
}
