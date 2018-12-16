package com.asus.zenmotions.settings;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;

public class DeviceSettingsActivity extends Activity {
    private DeviceSettings mDeviceSettingsFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Fragment fragment = getFragmentManager().findFragmentById(16908290);
        if (fragment == null) {
            this.mDeviceSettingsFragment = new DeviceSettings();
            getFragmentManager().beginTransaction().add(16908290, this.mDeviceSettingsFragment).commit();
            return;
        }
        this.mDeviceSettingsFragment = (DeviceSettings) fragment;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }
}
