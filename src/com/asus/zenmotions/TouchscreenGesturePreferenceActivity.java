package com.asus.zenmotions;

import android.os.Bundle;
import com.android.settingslib.drawer.SettingsDrawerActivity;

public class TouchscreenGesturePreferenceActivity extends SettingsDrawerActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, new TouchscreenGesturePreferenceFragment()).commit();
    }
}
