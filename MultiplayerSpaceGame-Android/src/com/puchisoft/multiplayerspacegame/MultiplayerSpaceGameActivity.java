package com.puchisoft.multiplayerspacegame;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.puchisoft.multiplayerspacegame.screen.WaoGame;

public class MultiplayerSpaceGameActivity extends AndroidApplication {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize(new WaoGame(), true);
    }
}