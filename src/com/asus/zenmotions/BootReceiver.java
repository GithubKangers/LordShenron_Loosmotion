package com.asus.zenmotions;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.preference.PreferenceManager;
import com.asus.zenmotions.kcal.DisplayCalibration;
import com.asus.zenmotions.settings.ScreenOffGesture;

public class BootReceiver extends BroadcastReceiver {
    private void restore(String file, boolean enabled) {
        if (file != null && enabled) {
            Utils.writeValue(file, "1");
        }
    }

    private void restore(String file, String value) {
        if (file != null) {
            Utils.writeValue(file, value);
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            enableComponent(context, ScreenOffGesture.class.getName());
            KernelControl.enableGestures(context.getSharedPreferences(ScreenOffGesture.GESTURE_SETTINGS, 0).getBoolean(ScreenOffGesture.PREF_GESTURE_ENABLE, true));
        }
        context.startService(new Intent(context, SensorsDozeService.class));
        context.startService(new Intent(context, DisplayCalibration.class));
    }

    private String getPreferenceString(Context context, String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);
    }

    private boolean getPreferenceBoolean(Context context, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
    }

    private void disableComponent(Context context, String component) {
        context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, component), 2, 1);
    }

    private void enableComponent(Context context, String component) {
        ComponentName name = new ComponentName(context, component);
        PackageManager pm = context.getPackageManager();
        if (pm.getComponentEnabledSetting(name) == 2) {
            pm.setComponentEnabledSetting(name, 1, 1);
        }
    }
}
