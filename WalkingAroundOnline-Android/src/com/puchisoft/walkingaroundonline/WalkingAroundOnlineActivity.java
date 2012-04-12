package com.puchisoft.walkingaroundonline;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.puchisoft.WalkingAroundOnline;

public class WalkingAroundOnlineActivity extends AndroidApplication {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize(new WalkingAroundOnline(), true);
    }
}