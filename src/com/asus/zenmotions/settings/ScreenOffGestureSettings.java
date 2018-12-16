package com.asus.zenmotions.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class ScreenOffGestureSettings extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(16908290, new ScreenOffGesture()).commit();
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }
}
