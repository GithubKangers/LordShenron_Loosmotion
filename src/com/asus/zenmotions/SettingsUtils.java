package com.asus.zenmotions;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

public class SettingsUtils {
    public static final String EXTRA_SHOW_FRAGMENT_AS_SUBSETTING = ":settings:show_fragment_as_subsetting";
    public static final String HIGH_TOUCH_SENSITIVITY_ENABLE = "HIGH_TOUCH_SENSITIVITY_ENABLE";
    public static final String LIGHTS_EFFECTS_MUSIC_ALWAYS = "LIGHTS_EFFECTS_MUSIC_ALWAYS";
    public static final String LIGHTS_EFFECTS_MUSIC_AWAKE = "LIGHTS_EFFECTS_MUSIC_AWAKE";
    public static final String LIGHTS_EFFECTS_MUSIC_ENABLE = "LIGHTS_EFFECTS_MUSIC_ENABLE";
    public static final String LIGHTS_EFFECTS_MUSIC_GAIN = "LIGHTS_EFFECTS_MUSIC_GAIN";
    public static final String PREFERENCES = "SettingsUtilsPreferences";
    public static final String SETTINGS_CLASS = "lineageos.providers.LineageSettings$System";
    public static final String TAG = "SettingsUtils";
    public static final String TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK = "TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK";

    public static int getIntSystem(Context context, ContentResolver cr, String name, int def) {
        try {
            Class systemSettings = Class.forName(SETTINGS_CLASS);
            String sdkName = (String) systemSettings.getDeclaredField(name).get(null);
            return ((Integer) systemSettings.getMethod("getInt", new Class[]{ContentResolver.class, String.class, Integer.TYPE}).invoke(systemSettings, new Object[]{cr, sdkName, Integer.valueOf(def)})).intValue();
        } catch (Exception e) {
            Log.i(TAG, "CMSettings not found. Using application settings for getInt");
            return getInt(context, name, def);
        }
    }

    public static int getInt(Context context, String name, int def) {
        return context.getSharedPreferences(PREFERENCES, 0).getInt(name, def);
    }

    public static boolean putIntSystem(Context context, ContentResolver cr, String name, int value) {
        try {
            Class systemSettings = Class.forName(SETTINGS_CLASS);
            String sdkName = (String) systemSettings.getDeclaredField(name).get(null);
            return ((Boolean) systemSettings.getMethod("putInt", new Class[]{ContentResolver.class, String.class, Integer.TYPE}).invoke(systemSettings, new Object[]{cr, sdkName, Integer.valueOf(value)})).booleanValue();
        } catch (Exception e) {
            Log.i(TAG, "CMSettings not found. Using application settings for putInt");
            return putInt(context, name, value);
        }
    }

    public static boolean putInt(Context context, String name, int value) {
        Editor editor = context.getSharedPreferences(PREFERENCES, 0).edit();
        editor.putInt(name, value);
        return editor.commit();
    }

    public static void registerPreferenceChangeListener(Context context, OnSharedPreferenceChangeListener preferenceListener) {
        context.getSharedPreferences(PREFERENCES, 0).registerOnSharedPreferenceChangeListener(preferenceListener);
    }

    public static void unregisterPreferenceChangeListener(Context context, OnSharedPreferenceChangeListener preferenceListener) {
        context.getSharedPreferences(PREFERENCES, 0).unregisterOnSharedPreferenceChangeListener(preferenceListener);
    }
}
