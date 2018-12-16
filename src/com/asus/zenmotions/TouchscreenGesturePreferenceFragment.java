package com.asus.zenmotions;

import android.app.ActionBar;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.AndroidResources;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class TouchscreenGesturePreferenceFragment extends PreferenceFragment {
    private static final String KEY_GESTURE_HAND_WAVE = "gesture_hand_wave";
    private static final String KEY_GESTURE_PICK_UP = "gesture_pick_up";
    private static final String KEY_GESTURE_POCKET = "gesture_pocket";
    private static final String KEY_HAPTIC_FEEDBACK = "touchscreen_gesture_haptic_feedback";
    private static final String KEY_PROXIMITY_WAKE = "proximity_wake_enable";
    private OnCheckedChangeListener mAmbientDisplayPrefListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton compoundButton, boolean enable) {
            if (TouchscreenGesturePreferenceFragment.this.enableDoze(enable)) {
                int i;
                TouchscreenGesturePreferenceFragment.this.mHandwavePreference.setEnabled(enable);
                TouchscreenGesturePreferenceFragment.this.mPickupPreference.setEnabled(enable);
                TouchscreenGesturePreferenceFragment.this.mPocketPreference.setEnabled(enable);
                TextView access$500 = TouchscreenGesturePreferenceFragment.this.mSwitchBarText;
                if (enable) {
                    i = R.string.switch_bar_on;
                } else {
                    i = R.string.switch_bar_off;
                }
                access$500.setText(i);
            }
        }
    };
    private Switch mAmbientDisplaySwitch;
    private OnPreferenceChangeListener mGesturePrefListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (((Boolean) newValue).booleanValue()) {
                String key = preference.getKey();
                if (TouchscreenGesturePreferenceFragment.KEY_GESTURE_HAND_WAVE.equals(key)) {
                    TouchscreenGesturePreferenceFragment.this.mProximityWakePreference.setChecked(false);
                } else if (TouchscreenGesturePreferenceFragment.KEY_PROXIMITY_WAKE.equals(key)) {
                    TouchscreenGesturePreferenceFragment.this.mHandwavePreference.setChecked(false);
                }
            }
            return true;
        }
    };
    private SwitchPreference mHandwavePreference;
    private SwitchPreference mHapticFeedback;
    private OnPreferenceChangeListener mHapticPrefListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (!TouchscreenGesturePreferenceFragment.KEY_HAPTIC_FEEDBACK.equals(preference.getKey())) {
                return false;
            }
            SettingsUtils.putIntSystem(TouchscreenGesturePreferenceFragment.this.getContext(), TouchscreenGesturePreferenceFragment.this.getActivity().getContentResolver(), SettingsUtils.TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK, ((Boolean) newValue).booleanValue());
            return true;
        }
    };
    private SwitchPreference mPickupPreference;
    private SwitchPreference mPocketPreference;
    private SwitchPreference mProximityWakePreference;
    private TextView mSwitchBarText;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionbar = getActivity().getActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setTitle(R.string.ambient_display_title);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.doze, container, false);
        ((ViewGroup) view).addView(super.onCreateView(inflater, container, savedInstanceState));
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        int i;
        super.onViewCreated(view, savedInstanceState);
        boolean dozeEnabled = isDozeEnabled();
        View switchBar = view.findViewById(R.id.switch_bar);
        this.mAmbientDisplaySwitch = (Switch) switchBar.findViewById(AndroidResources.ANDROID_R_SWITCH_WIDGET);
        this.mAmbientDisplaySwitch.setChecked(dozeEnabled);
        this.mAmbientDisplaySwitch.setOnCheckedChangeListener(this.mAmbientDisplayPrefListener);
        switchBar.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TouchscreenGesturePreferenceFragment.this.mAmbientDisplaySwitch.toggle();
            }
        });
        this.mSwitchBarText = (TextView) switchBar.findViewById(R.id.switch_text);
        TextView textView = this.mSwitchBarText;
        if (dozeEnabled) {
            i = R.string.switch_bar_on;
        } else {
            i = R.string.switch_bar_off;
        }
        textView.setText(i);
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.gesture_panel);
        boolean dozeEnabled = isDozeEnabled();
        this.mHandwavePreference = (SwitchPreference) findPreference(KEY_GESTURE_HAND_WAVE);
        this.mHandwavePreference.setEnabled(dozeEnabled);
        this.mHandwavePreference.setOnPreferenceChangeListener(this.mGesturePrefListener);
        this.mPickupPreference = (SwitchPreference) findPreference(KEY_GESTURE_PICK_UP);
        this.mPickupPreference.setEnabled(dozeEnabled);
        this.mPocketPreference = (SwitchPreference) findPreference(KEY_GESTURE_POCKET);
        this.mPocketPreference.setEnabled(dozeEnabled);
        this.mProximityWakePreference = (SwitchPreference) findPreference(KEY_PROXIMITY_WAKE);
        this.mProximityWakePreference.setOnPreferenceChangeListener(this.mGesturePrefListener);
        this.mHapticFeedback = (SwitchPreference) findPreference(KEY_HAPTIC_FEEDBACK);
        this.mHapticFeedback.setOnPreferenceChangeListener(this.mHapticPrefListener);
    }

    public void onResume() {
        super.onResume();
        SwitchPreference switchPreference = this.mHapticFeedback;
        boolean z = true;
        if (SettingsUtils.getIntSystem(getContext(), getActivity().getContentResolver(), SettingsUtils.TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK, 1) == 0) {
            z = false;
        }
        switchPreference.setChecked(z);
        getListView().setPadding(0, 0, 0, 0);
    }

    private boolean enableDoze(boolean enable) {
        return Secure.putInt(getActivity().getContentResolver(), "doze_enabled", enable);
    }

    private boolean isDozeEnabled() {
        return Secure.getInt(getActivity().getContentResolver(), "doze_enabled", 1) != 0;
    }
}
