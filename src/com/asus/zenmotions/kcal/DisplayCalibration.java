package com.asus.zenmotions.kcal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import com.asus.zenmotions.R;
import com.asus.zenmotions.kcal.utils.SeekBarPreference;

public class DisplayCalibration extends PreferenceActivity implements OnPreferenceChangeListener {
    private static final String COLOR_FILE = "/sys/devices/platform/kcal_ctrl.0/kcal";
    private static final String COLOR_FILE_CONTRAST = "/sys/devices/platform/kcal_ctrl.0/kcal_cont";
    private static final String COLOR_FILE_ENABLE = "/sys/devices/platform/kcal_ctrl.0/kcal_enable";
    private static final String COLOR_FILE_SATURATION = "/sys/devices/platform/kcal_ctrl.0/kcal_sat";
    public static final String KEY_KCAL_BLUE = "kcal_blue";
    public static final String KEY_KCAL_COLOR_TEMP = "kcal_color_temp";
    public static final String KEY_KCAL_CONTRAST = "kcal_contrast";
    public static final String KEY_KCAL_ENABLED = "kcal_enabled";
    public static final String KEY_KCAL_GREEN = "kcal_green";
    public static final String KEY_KCAL_RED = "kcal_red";
    public static final String KEY_KCAL_SATURATION = "kcal_saturation";
    private String mBlue;
    private boolean mEnabled;
    private String mGreen;
    private SeekBarPreference mKcalBlue;
    private SeekBarPreference mKcalColorTemp;
    private SeekBarPreference mKcalContrast;
    private SwitchPreference mKcalEnabled;
    private SeekBarPreference mKcalGreen;
    private SeekBarPreference mKcalRed;
    private SeekBarPreference mKcalSaturation;
    private SharedPreferences mPrefs;
    private String mRed;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.display_cal);
        ((ImageView) findViewById(R.id.calibration_pic)).setImageResource(R.drawable.calibration_png);
        addPreferencesFromResource(R.xml.display_calibration);
        this.mKcalEnabled = (SwitchPreference) findPreference(KEY_KCAL_ENABLED);
        this.mKcalEnabled.setChecked(this.mPrefs.getBoolean(KEY_KCAL_ENABLED, false));
        this.mKcalEnabled.setOnPreferenceChangeListener(this);
        this.mKcalRed = (SeekBarPreference) findPreference(KEY_KCAL_RED);
        this.mKcalRed.setInitValue(this.mPrefs.getInt(KEY_KCAL_RED, this.mKcalRed.def));
        this.mKcalRed.setOnPreferenceChangeListener(this);
        this.mKcalGreen = (SeekBarPreference) findPreference(KEY_KCAL_GREEN);
        this.mKcalGreen.setInitValue(this.mPrefs.getInt(KEY_KCAL_GREEN, this.mKcalGreen.def));
        this.mKcalGreen.setOnPreferenceChangeListener(this);
        this.mKcalBlue = (SeekBarPreference) findPreference(KEY_KCAL_BLUE);
        this.mKcalBlue.setInitValue(this.mPrefs.getInt(KEY_KCAL_BLUE, this.mKcalBlue.def));
        this.mKcalBlue.setOnPreferenceChangeListener(this);
        this.mKcalSaturation = (SeekBarPreference) findPreference(KEY_KCAL_SATURATION);
        this.mKcalSaturation.setInitValue(this.mPrefs.getInt(KEY_KCAL_SATURATION, this.mKcalSaturation.def));
        this.mKcalSaturation.setOnPreferenceChangeListener(this);
        this.mKcalContrast = (SeekBarPreference) findPreference(KEY_KCAL_CONTRAST);
        this.mKcalContrast.setInitValue(this.mPrefs.getInt(KEY_KCAL_CONTRAST, this.mKcalContrast.def));
        this.mKcalContrast.setOnPreferenceChangeListener(this);
        this.mKcalColorTemp = (SeekBarPreference) findPreference(KEY_KCAL_COLOR_TEMP);
        this.mKcalColorTemp.setInitValue(this.mPrefs.getInt(KEY_KCAL_COLOR_TEMP, this.mKcalColorTemp.def));
        this.mKcalColorTemp.setOnPreferenceChangeListener(this);
        this.mRed = String.valueOf(this.mPrefs.getInt(KEY_KCAL_RED, this.mKcalRed.def));
        this.mGreen = String.valueOf(this.mPrefs.getInt(KEY_KCAL_GREEN, this.mKcalGreen.def));
        this.mBlue = String.valueOf(this.mPrefs.getInt(KEY_KCAL_BLUE, this.mKcalBlue.def));
    }

    private boolean isSupported(String file) {
        return Utils.fileWritable(file);
    }

    public static void restore(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_KCAL_ENABLED, false)) {
            Utils.writeValue(COLOR_FILE_ENABLE, "1");
            Utils.writeValue(COLOR_FILE, "1");
            int storedRed = PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_KCAL_RED, 256);
            int storedGreen = PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_KCAL_GREEN, 256);
            int storedBlue = PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_KCAL_BLUE, 256);
            int storedSaturation = PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_KCAL_SATURATION, 255);
            int storedContrast = PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_KCAL_CONTRAST, 255);
            String storedValue = new StringBuilder();
            storedValue.append(String.valueOf(storedRed));
            storedValue.append(" ");
            storedValue.append(String.valueOf(storedGreen));
            storedValue.append(" ");
            storedValue.append(String.valueOf(storedBlue));
            Utils.writeValue(COLOR_FILE, storedValue.toString());
            Utils.writeValue(COLOR_FILE_CONTRAST, String.valueOf(storedContrast));
            Utils.writeValue(COLOR_FILE_SATURATION, String.valueOf(storedSaturation));
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.kcal_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == 16908332) {
            finish();
            return true;
        } else if (itemId != R.id.menu_reset) {
            return super.onOptionsItemSelected(item);
        } else {
            reset();
            return true;
        }
    }

    public void reset() {
        int red = this.mKcalRed.reset();
        int green = this.mKcalGreen.reset();
        int blue = this.mKcalBlue.reset();
        int saturation = this.mKcalSaturation.reset();
        int contrast = this.mKcalContrast.reset();
        this.mPrefs.edit().putInt(KEY_KCAL_RED, red).commit();
        this.mPrefs.edit().putInt(KEY_KCAL_GREEN, green).commit();
        this.mPrefs.edit().putInt(KEY_KCAL_BLUE, blue).commit();
        this.mPrefs.edit().putInt(KEY_KCAL_SATURATION, saturation).commit();
        this.mPrefs.edit().putInt(KEY_KCAL_CONTRAST, contrast).commit();
        String storedValue = new StringBuilder();
        storedValue.append(Integer.toString(red));
        storedValue.append(" ");
        storedValue.append(Integer.toString(green));
        storedValue.append(" ");
        storedValue.append(Integer.toString(blue));
        Utils.writeValue(COLOR_FILE, storedValue.toString());
        Utils.writeValue(COLOR_FILE_SATURATION, Integer.toString(saturation));
        Utils.writeValue(COLOR_FILE_CONTRAST, Integer.toString(contrast));
        this.mKcalColorTemp.setValue(Utils.KfromRGB((double) this.mPrefs.getInt(KEY_KCAL_RED, 256), (double) this.mPrefs.getInt(KEY_KCAL_GREEN, 256), (double) this.mPrefs.getInt(KEY_KCAL_BLUE, 256)));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String storedValue;
        float val;
        StringBuilder stringBuilder;
        if (preference == this.mKcalEnabled) {
            Boolean enabled = (Boolean) newValue;
            this.mPrefs.edit().putBoolean(KEY_KCAL_ENABLED, enabled.booleanValue()).commit();
            this.mRed = String.valueOf(this.mPrefs.getInt(KEY_KCAL_RED, 256));
            this.mBlue = String.valueOf(this.mPrefs.getInt(KEY_KCAL_BLUE, 256));
            this.mGreen = String.valueOf(this.mPrefs.getInt(KEY_KCAL_GREEN, 256));
            storedValue = new StringBuilder();
            storedValue.append(String.valueOf(this.mRed));
            storedValue.append(" ");
            storedValue.append(String.valueOf(this.mGreen));
            storedValue.append(" ");
            storedValue.append(String.valueOf(this.mBlue));
            storedValue = storedValue.toString();
            String mSaturation = String.valueOf(this.mPrefs.getInt(KEY_KCAL_SATURATION, 256));
            String mContrast = String.valueOf(this.mPrefs.getInt(KEY_KCAL_CONTRAST, 256));
            Utils.writeValue(COLOR_FILE_ENABLE, enabled.booleanValue() ? "1" : "0");
            Utils.writeValue(COLOR_FILE, storedValue);
            Utils.writeValue(COLOR_FILE_SATURATION, mSaturation);
            Utils.writeValue(COLOR_FILE_CONTRAST, mContrast);
            this.mKcalColorTemp.setValue(Utils.KfromRGB((double) this.mPrefs.getInt(KEY_KCAL_RED, 256), (double) this.mPrefs.getInt(KEY_KCAL_GREEN, 256), (double) this.mPrefs.getInt(KEY_KCAL_BLUE, 256)));
            return true;
        } else if (preference == this.mKcalRed) {
            val = Float.parseFloat((String) newValue);
            this.mPrefs.edit().putInt(KEY_KCAL_RED, (int) val).commit();
            this.mGreen = String.valueOf(this.mPrefs.getInt(KEY_KCAL_GREEN, 256));
            this.mBlue = String.valueOf(this.mPrefs.getInt(KEY_KCAL_BLUE, 256));
            storedValue = new StringBuilder();
            storedValue.append((String) newValue);
            storedValue.append(" ");
            storedValue.append(this.mGreen);
            storedValue.append(" ");
            storedValue.append(this.mBlue);
            Utils.writeValue(COLOR_FILE, storedValue.toString());
            this.mKcalColorTemp.setValue(Utils.KfromRGB((double) val, (double) this.mPrefs.getInt(KEY_KCAL_GREEN, 256), (double) this.mPrefs.getInt(KEY_KCAL_BLUE, 256)));
            return true;
        } else if (preference == this.mKcalGreen) {
            val = Float.parseFloat((String) newValue);
            this.mPrefs.edit().putInt(KEY_KCAL_GREEN, (int) val).commit();
            this.mRed = String.valueOf(this.mPrefs.getInt(KEY_KCAL_RED, 256));
            this.mBlue = String.valueOf(this.mPrefs.getInt(KEY_KCAL_BLUE, 256));
            stringBuilder = new StringBuilder();
            stringBuilder.append(this.mRed);
            stringBuilder.append(" ");
            stringBuilder.append(newValue);
            stringBuilder.append(" ");
            stringBuilder.append(this.mBlue);
            Utils.writeValue(COLOR_FILE, stringBuilder.toString());
            this.mKcalColorTemp.setValue(Utils.KfromRGB((double) this.mPrefs.getInt(KEY_KCAL_RED, 256), (double) val, (double) this.mPrefs.getInt(KEY_KCAL_BLUE, 256)));
            return true;
        } else if (preference == this.mKcalBlue) {
            val = Float.parseFloat((String) newValue);
            this.mPrefs.edit().putInt(KEY_KCAL_BLUE, (int) val).commit();
            this.mRed = String.valueOf(this.mPrefs.getInt(KEY_KCAL_RED, 256));
            this.mGreen = String.valueOf(this.mPrefs.getInt(KEY_KCAL_GREEN, 256));
            stringBuilder = new StringBuilder();
            stringBuilder.append(this.mRed);
            stringBuilder.append(" ");
            stringBuilder.append(this.mGreen);
            stringBuilder.append(" ");
            stringBuilder.append(newValue);
            Utils.writeValue(COLOR_FILE, stringBuilder.toString());
            this.mKcalColorTemp.setValue(Utils.KfromRGB((double) this.mPrefs.getInt(KEY_KCAL_RED, 256), (double) this.mPrefs.getInt(KEY_KCAL_GREEN, 256), (double) val));
            return true;
        } else if (preference == this.mKcalSaturation) {
            this.mPrefs.edit().putInt(KEY_KCAL_SATURATION, (int) Float.parseFloat((String) newValue)).commit();
            Utils.writeValue(COLOR_FILE_SATURATION, (String) newValue);
            return true;
        } else if (preference == this.mKcalContrast) {
            this.mPrefs.edit().putInt(KEY_KCAL_CONTRAST, (int) Float.parseFloat((String) newValue)).commit();
            Utils.writeValue(COLOR_FILE_CONTRAST, (String) newValue);
            return true;
        } else {
            if (preference == this.mKcalColorTemp) {
                int[] colorTemp = Utils.RGBfromK(Integer.parseInt((String) newValue));
                int red = colorTemp[0];
                int green = colorTemp[1];
                int blue = colorTemp[2];
                this.mKcalRed.setValue(red);
                this.mKcalGreen.setValue(green);
                this.mKcalBlue.setValue(blue);
                this.mPrefs.edit().putInt(KEY_KCAL_RED, red).commit();
                this.mPrefs.edit().putInt(KEY_KCAL_GREEN, green).commit();
                this.mPrefs.edit().putInt(KEY_KCAL_BLUE, blue).commit();
                String storedValue2 = new StringBuilder();
                storedValue2.append(Integer.toString(red));
                storedValue2.append(" ");
                storedValue2.append(Integer.toString(green));
                storedValue2.append(" ");
                storedValue2.append(Integer.toString(blue));
                Utils.writeValue(COLOR_FILE, storedValue2.toString());
            }
            return false;
        }
    }
}
