package com.asus.zenmotions;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import com.asus.zenmotions.kcal.DisplayCalibration;
import com.asus.zenmotions.settings.ScreenOffGestureSettings;

public class zenmotions extends PreferenceActivity implements OnPreferenceChangeListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "Zenmotions";
    private Preference mAmbientPref;
    private Context mContext;
    private Preference mGesturesPref;
    private Preference mKcalPref;
    private SharedPreferences mPreferences;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.zenmotions);
        this.mGesturesPref = findPreference("zenmotions");
        this.mGesturesPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                zenmotions.this.startActivity(new Intent(zenmotions.this.getApplicationContext(), ScreenOffGestureSettings.class));
                return true;
            }
        });
        this.mAmbientPref = findPreference("ambient_display_gestures");
        this.mAmbientPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                zenmotions.this.startActivity(new Intent(zenmotions.this.getApplicationContext(), TouchscreenGesturePreferenceActivity.class));
                return true;
            }
        });
        this.mKcalPref = findPreference("kcal");
        this.mKcalPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                zenmotions.this.startActivity(new Intent(zenmotions.this.getApplicationContext(), DisplayCalibration.class));
                return true;
            }
        });
        this.mContext = getApplicationContext();
    }

    protected void onResume() {
        super.onResume();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return false;
        }
        onBackPressed();
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        return true;
    }
}
