package com.puchisoft.multiplayerspacegame;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.esotericsoftware.minlog.Log;

public class GameSounds {
	
	private Map<String, Sound> sounds = new HashMap<String, Sound>();
	// These sounds will be loaded
	private final String[] soundNames = {"shoot", "turn", "accel","boost"};
	
	public GameSounds(){
		for(String name: soundNames){
			sounds.put(name, Gdx.audio.newSound(Gdx.files.internal("data/"+name+".wav")));
		}
	}
	
	public void playAndLoopSound(String name){
		Sound sound = sounds.get(name);
		if(sound == null){
			Log.error("Can't find sound: "+name);
		}else{
			long soundId = sound.play();
			sound.setLooping(soundId, true);
		}
	}
	
	public void stopSound(String name){
		Sound sound = sounds.get(name);
		if(sound == null){
			Log.error("Can't find sound: "+name);
		}else{
			sound.stop();
		}
	}
	
	public void stopAllSounds(){
		for(Map.Entry<String, Sound> soundEntry : sounds.entrySet()){
			soundEntry.getValue().stop();
		}
	}
	
	public void dispose(){
		for(Map.Entry<String, Sound> soundEntry : sounds.entrySet()){
			soundEntry.getValue().dispose();
		}
	}
}
