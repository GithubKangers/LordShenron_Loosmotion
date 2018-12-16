package com.asus.zenmotions.settings;

import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import com.asus.zenmotions.R;
import com.asus.zenmotions.util.FileUtils;

public class DeviceSettings extends PreferenceFragment implements OnPreferenceChangeListener {
    public static final String BUTTON_EXTRA_KEY_MAPPING = "/sys/devices/virtual/switch/tri-state-key/state";
    public static final String KEYCODE_SLIDER_BOTTOM = "slider_bottom";
    public static final String KEYCODE_SLIDER_MIDDLE = "slider_middle";
    public static final String KEYCODE_SLIDER_TOP = "slider_top";
    private static final String KEY_CATEGORY_GRAPHICS = "graphics";
    public static final String KEY_DCI_SWITCH = "dci";
    public static final String KEY_PROXI_SWITCH = "proxi";
    public static final String KEY_SRGB_SWITCH = "srgb";
    public static final String SLIDER_DEFAULT_VALUE = "5,1,0";
    public static final String SLIDER_SWAP_NODE = "/proc/s1302/key_rep";
    final String KEY_DEVICE_DOZE = "device_doze";
    final String KEY_DEVICE_DOZE_PACKAGE_NAME = "org.lineageos.settings.doze";
    private TwoStatePreference mDCIModeSwitch;
    private TwoStatePreference mProxiSwitch;
    private TwoStatePreference mSRGBModeSwitch;
    private ListPreference mSliderModeBottom;
    private ListPreference mSliderModeCenter;
    private ListPreference mSliderModeTop;
    private TwoStatePreference mSliderSwap;

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main, rootKey);
        this.mSliderSwap = (TwoStatePreference) findPreference("button_swap");
        this.mSliderSwap.setOnPreferenceChangeListener(this);
        this.mSliderModeTop = (ListPreference) findPreference(KEYCODE_SLIDER_TOP);
        this.mSliderModeTop.setOnPreferenceChangeListener(this);
        boolean z = false;
        int valueIndex = this.mSliderModeTop.findIndexOfValue(String.valueOf(getSliderAction(0)));
        this.mSliderModeTop.setValueIndex(valueIndex);
        this.mSliderModeTop.setSummary(this.mSliderModeTop.getEntries()[valueIndex]);
        this.mSliderModeCenter = (ListPreference) findPreference(KEYCODE_SLIDER_MIDDLE);
        this.mSliderModeCenter.setOnPreferenceChangeListener(this);
        valueIndex = this.mSliderModeCenter.findIndexOfValue(String.valueOf(getSliderAction(1)));
        this.mSliderModeCenter.setValueIndex(valueIndex);
        this.mSliderModeCenter.setSummary(this.mSliderModeCenter.getEntries()[valueIndex]);
        this.mSliderModeBottom = (ListPreference) findPreference(KEYCODE_SLIDER_BOTTOM);
        this.mSliderModeBottom.setOnPreferenceChangeListener(this);
        valueIndex = this.mSliderModeBottom.findIndexOfValue(String.valueOf(getSliderAction(2)));
        this.mSliderModeBottom.setValueIndex(valueIndex);
        this.mSliderModeBottom.setSummary(this.mSliderModeBottom.getEntries()[valueIndex]);
        this.mProxiSwitch = (TwoStatePreference) findPreference(KEY_PROXI_SWITCH);
        TwoStatePreference twoStatePreference = this.mProxiSwitch;
        if (System.getInt(getContext().getContentResolver(), "device_proxi_check_enabled", 1) != 0) {
            z = true;
        }
        twoStatePreference.setChecked(z);
    }

    private void setSummary(ListPreference preference, String file) {
        String readOneLine = FileUtils.readOneLine(file);
        String keyCode = readOneLine;
        if (readOneLine != null) {
            preference.setValue(keyCode);
            preference.setSummary(preference.getEntry());
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference != this.mProxiSwitch) {
            return super.onPreferenceTreeClick(preference);
        }
        System.putInt(getContext().getContentResolver(), "device_proxi_check_enabled", this.mProxiSwitch.isChecked());
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == this.mSliderSwap) {
            FileUtils.writeLine("/proc/s1302/key_rep", ((Boolean) newValue).booleanValue() ? "1" : "0");
        }
        String value;
        if (preference == this.mSliderModeTop) {
            value = (String) newValue;
            setSliderAction(0, Integer.valueOf(value).intValue());
            this.mSliderModeTop.setSummary(this.mSliderModeTop.getEntries()[this.mSliderModeTop.findIndexOfValue(value)]);
        } else if (preference == this.mSliderModeCenter) {
            value = (String) newValue;
            setSliderAction(1, Integer.valueOf(value).intValue());
            this.mSliderModeCenter.setSummary(this.mSliderModeCenter.getEntries()[this.mSliderModeCenter.findIndexOfValue(value)]);
        } else if (preference == this.mSliderModeBottom) {
            value = (String) newValue;
            setSliderAction(2, Integer.valueOf(value).intValue());
            this.mSliderModeBottom.setSummary(this.mSliderModeBottom.getEntries()[this.mSliderModeBottom.findIndexOfValue(value)]);
        }
        return true;
    }

    private int getSliderAction(int position) {
        String value = System.getString(getContext().getContentResolver(), BUTTON_EXTRA_KEY_MAPPING);
        String defaultValue = SLIDER_DEFAULT_VALUE;
        if (value == null) {
            value = SLIDER_DEFAULT_VALUE;
        } else if (value.indexOf(",") == -1) {
            value = SLIDER_DEFAULT_VALUE;
        }
        try {
            return Integer.valueOf(value.split(",")[position]).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    private void setSliderAction(int position, int action) {
        String value = System.getString(getContext().getContentResolver(), BUTTON_EXTRA_KEY_MAPPING);
        String defaultValue = SLIDER_DEFAULT_VALUE;
        if (value == null) {
            value = SLIDER_DEFAULT_VALUE;
        } else if (value.indexOf(",") == -1) {
            value = SLIDER_DEFAULT_VALUE;
        }
        try {
            String[] parts = value.split(",");
            parts[position] = String.valueOf(action);
            System.putString(getContext().getContentResolver(), BUTTON_EXTRA_KEY_MAPPING, TextUtils.join(",", parts));
        } catch (Exception e) {
        }
    }
}
