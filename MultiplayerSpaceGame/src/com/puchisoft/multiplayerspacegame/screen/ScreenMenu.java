package com.puchisoft.multiplayerspacegame.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;

public class ScreenMenu extends ScreenCore {
	Texture title;
	SpriteBatch batch;
	float time = 0;
	
	Skin skin;
	Stage stage;
	TextField textfieldIP;

	public ScreenMenu(Game game) {
		super(game);
	}

	@Override
	public void show() {
		title = new Texture(Gdx.files.internal("data/menu.png"));
		batch = new SpriteBatch();
		
		skin = new Skin(Gdx.files.internal("data/uiskin.json"), Gdx.files.internal("data/uiskin.png"));
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		
		// Create UI elements
		final Label labelTitle = new Label("Multiplayer Space Game", skin);
		labelTitle.setColor(Color.RED);
		labelTitle.y = Gdx.graphics.getHeight() - labelTitle.height - 10;
		labelTitle.x = Gdx.graphics.getWidth()/2 - labelTitle.width/2;
		
		textfieldIP = new TextField("puchisoft.servegame.com", "Enter IP", skin.getStyle(TextFieldStyle.class), "textfield_ip");
		textfieldIP.width = 200;
		textfieldIP.height = 30;
		textfieldIP.y = Gdx.graphics.getHeight() - textfieldIP.height - 65 -200;
		textfieldIP.x = Gdx.graphics.getWidth()/2 - textfieldIP.width/2;
		
		final Button buttonJoin = new TextButton("Join Game", skin.getStyle(TextButtonStyle.class), "button_join");
		buttonJoin.width = 200;
		buttonJoin.height = 100;
		buttonJoin.y = Gdx.graphics.getHeight() - buttonJoin.height - 100 -200;
		buttonJoin.x = Gdx.graphics.getWidth()/2-buttonJoin.width/2;
		
		final Button buttonHost = new TextButton("Host Game", skin.getStyle(TextButtonStyle.class), "button_join");
		buttonHost.width = 200;
		buttonHost.height = 100;
		buttonHost.y = Gdx.graphics.getHeight() - buttonHost.height - 220 -200;
		buttonHost.x = Gdx.graphics.getWidth()/2-buttonHost.width/2;
		
		
		stage.addActor(labelTitle);
		stage.addActor(textfieldIP);
		stage.addActor(buttonJoin);
		stage.addActor(buttonHost);
		
		// Events
		textfieldIP.setTextFieldListener(new TextFieldListener() {
			public void keyTyped (TextField textField, char key) {
				if (key == '\n') textField.getOnscreenKeyboard().show(false);
			}
		});
		buttonJoin.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				goJoin();
				
			}
		});
		buttonHost.setClickListener(new ClickListener() {
			@Override
			public void click(Actor actor, float x, float y) {
				goHost();
				
			}
		});
		
		// Actually get events
		Gdx.input.setInputProcessor(stage);
	}
	
	private void goJoin(){
		game.setScreen(new ScreenGame(game, false, textfieldIP.getText()));
	}
	private void goHost(){
		game.setScreen(new ScreenGame(game, true, "localhost"));
	}
	

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(title, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.end();
		
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();

		if (Gdx.input.isKeyPressed(Keys.ALT_LEFT)) {
			goHost();
		}
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
		stage.dispose();
		skin.dispose();
		batch.dispose();
		title.dispose();
	}
}
