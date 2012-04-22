package com.puchisoft.multiplayerspacegame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Moon {

	private Texture texture;
	public Vector2 position;
	
	public Moon(Texture texture, Vector2 position) {
		this.texture = texture;
		this.position = position;
	}

	public void render(SpriteBatch spriteBatch) {
		// TODO Auto-generated method stub
		spriteBatch.draw(texture, position.x, position.y, 0, 0, 
				texture.getWidth(), texture.getHeight());
	}
	
}
