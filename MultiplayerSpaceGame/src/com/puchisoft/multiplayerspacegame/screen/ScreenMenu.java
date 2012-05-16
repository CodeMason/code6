package com.puchisoft.multiplayerspacegame.screen;

import java.util.Random;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
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
	private Texture title;
	private SpriteBatch batch;
	
	private Skin skin;
	private Stage stage;
	private TextField textfieldIP;
	private TextField textfieldName;
	
	private Random random = new Random();
	Preferences preferences;

	public ScreenMenu(Game game) {
		super(game);
		preferences = Gdx.app.getPreferences("MultiplayerSpaceGame_Settings");
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
		
		textfieldIP = new TextField(preferences.getString("ip"), "Enter Server IP Here", skin.getStyle(TextFieldStyle.class), "textfield_ip");
		textfieldIP.width = 400;
		textfieldIP.height = 30;
		textfieldIP.y = Gdx.graphics.getHeight() - textfieldIP.height - 65 -200;
		textfieldIP.x = Gdx.graphics.getWidth()/2 - textfieldIP.width/2;
		
		textfieldName = new TextField(preferences.getString("name"), "Enter Name", skin.getStyle(TextFieldStyle.class), "textfield_name");
		textfieldName.width = 400;
		textfieldName.height = 30;
		textfieldName.y = Gdx.graphics.getHeight() - textfieldName.height - 0 -200;
		textfieldName.x = Gdx.graphics.getWidth()/2 - textfieldName.width/2;
		
//		final Button buttonFind = new TextButton("Find", skin.getStyle(TextButtonStyle.class), "button_join");
//		buttonFind.width = 60;
//		buttonFind.height = textfieldIP.height;
//		buttonFind.y = textfieldIP.y;
//		buttonFind.x = textfieldIP.x+textfieldIP.width+5;
		
		final Button buttonJoin = new TextButton("Join Game", skin.getStyle(TextButtonStyle.class), "button_join");
		buttonJoin.width = 400;
		buttonJoin.height = 100;
		buttonJoin.y = Gdx.graphics.getHeight() - buttonJoin.height - 100 -200;
		buttonJoin.x = Gdx.graphics.getWidth()/2-buttonJoin.width/2;
		
		final Button buttonHost = new TextButton("Host Game", skin.getStyle(TextButtonStyle.class), "button_join");
		buttonHost.width = 400;
		buttonHost.height = 100;
		buttonHost.y = Gdx.graphics.getHeight() - buttonHost.height - 220 -200;
		buttonHost.x = Gdx.graphics.getWidth()/2-buttonHost.width/2;
		
		
		stage.addActor(labelTitle);
		stage.addActor(textfieldIP);
		stage.addActor(textfieldName);
//		stage.addActor(buttonFind);
		stage.addActor(buttonJoin);
		stage.addActor(buttonHost);
		
		// Events
		textfieldIP.setTextFieldListener(new TextFieldListener() {
			public void keyTyped (TextField textField, char key) {
				if (key == '\n') textField.getOnscreenKeyboard().show(false);
			}
		});
//		buttonFind.setClickListener(new ClickListener() {
//			@Override
//			public void click(Actor actor, float x, float y) {
//				goFind();
//				
//			}
//		});
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
	
//	private void goFind() {
//		Client client = new Client();
//		client.start();
//		InetAddress found = client.discoverHost(Network.portUdp, 5000);
//		if(found != null){
//			textfieldIP.setText(found.getHostAddress().toString());
//		}
//		client.stop();
//		client.close();
//		
//	}
	
	private void savePrefs(){
		preferences.putString("name", getName());
		preferences.putString("ip", textfieldIP.getText());
		preferences.flush();
	}

	private void goJoin(){
		game.setScreen(new ScreenGame(game, false, textfieldIP.getText(), getName()));
		savePrefs();
	}
	private void goHost(){
		game.setScreen(new ScreenGame(game, true, "localhost", getName()));
		savePrefs();
	}
	
	private String getName(){
		String name = textfieldName.getText();
		if(name.isEmpty()){
			name = "Guest" + random.nextInt(10000); // default name
		}
		textfieldName.setText(name);
		return name;
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
