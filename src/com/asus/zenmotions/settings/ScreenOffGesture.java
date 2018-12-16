package com.asus.zenmotions.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settingslib.datetime.ZoneGetter;
import com.asus.zenmotions.KernelControl;
import com.asus.zenmotions.R;
import com.asus.zenmotions.util.ActionConstants;
import com.asus.zenmotions.util.AppHelper;
import com.asus.zenmotions.util.DeviceUtils;
import com.asus.zenmotions.util.DeviceUtils.FilteredDeviceFeaturesArray;
import com.asus.zenmotions.util.ShortcutPickerHelper;
import com.asus.zenmotions.util.ShortcutPickerHelper.OnPickListener;

public class ScreenOffGesture extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener, OnPickListener {
    private static final int DLG_RESET_TO_DEFAULT = 1;
    private static final int DLG_SHOW_ACTION_DIALOG = 0;
    public static final String GESTURE_SETTINGS = "screen_off_gesture_settings";
    private static final int MENU_RESET = 1;
    public static final String PREF_GESTURE_C = "gesture_c";
    public static final String PREF_GESTURE_DOUBLE_TAP = "gesture_double_tap";
    public static final String PREF_GESTURE_DOWN = "gesture_down";
    public static final String PREF_GESTURE_E = "gesture_e";
    public static final String PREF_GESTURE_ENABLE = "enable_gestures";
    public static final String PREF_GESTURE_LEFT = "gesture_left";
    public static final String PREF_GESTURE_RIGHT = "gesture_right";
    public static final String PREF_GESTURE_S = "gesture_s";
    public static final String PREF_GESTURE_UP = "gesture_up";
    public static final String PREF_GESTURE_V = "gesture_v";
    public static final String PREF_GESTURE_W = "gesture_w";
    public static final String PREF_GESTURE_Z = "gesture_z";
    private static final String SETTINGS_METADATA_NAME = "com.android.settings";
    private static FilteredDeviceFeaturesArray sFinalActionDialogArray;
    private boolean mCheckPreferences;
    private SwitchPreference mEnableGestures;
    private Preference mGestureArrowDown;
    private Preference mGestureArrowLeft;
    private Preference mGestureArrowRight;
    private Preference mGestureArrowUp;
    private Preference mGestureC;
    private Preference mGestureDoubleSwipe;
    private Preference mGestureDoubleTap;
    private Preference mGestureSwipeDown;
    private Preference mGestureSwipeLeft;
    private Preference mGestureSwipeRight;
    private Preference mGestureSwipeUp;
    private String mPendingSettingsKey;
    private ShortcutPickerHelper mPicker;
    private SharedPreferences mScreenOffGestureSharedPreferences;

    public static class MyAlertDialogFragment extends DialogFragment {
        public static MyAlertDialogFragment newInstance(int id, String settingsKey, int dialogTitle) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ZoneGetter.KEY_ID, id);
            args.putString("settingsKey", settingsKey);
            args.putInt("dialogTitle", dialogTitle);
            frag.setArguments(args);
            return frag;
        }

        ScreenOffGesture getOwner() {
            return (ScreenOffGesture) getTargetFragment();
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt(ZoneGetter.KEY_ID);
            final String settingsKey = getArguments().getString("settingsKey");
            int dialogTitle = getArguments().getInt("dialogTitle");
            switch (id) {
                case 0:
                    if (ScreenOffGesture.sFinalActionDialogArray == null) {
                        return null;
                    }
                    Builder negativeButton = new Builder(getActivity()).setTitle(dialogTitle).setNegativeButton(R.string.cancel, null);
                    getOwner();
                    return negativeButton.setItems(ScreenOffGesture.sFinalActionDialogArray.entries, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            MyAlertDialogFragment.this.getOwner();
                            if (!ScreenOffGesture.sFinalActionDialogArray.values[item].equals(ActionConstants.ACTION_APP)) {
                                Editor edit = MyAlertDialogFragment.this.getOwner().mScreenOffGestureSharedPreferences.edit();
                                String str = settingsKey;
                                MyAlertDialogFragment.this.getOwner();
                                edit.putString(str, ScreenOffGesture.sFinalActionDialogArray.values[item]).commit();
                                MyAlertDialogFragment.this.getOwner().reloadSettings();
                            } else if (MyAlertDialogFragment.this.getOwner().mPicker != null) {
                                MyAlertDialogFragment.this.getOwner().mPendingSettingsKey = settingsKey;
                                MyAlertDialogFragment.this.getOwner().mPicker.pickShortcut(MyAlertDialogFragment.this.getOwner().getId());
                            }
                        }
                    }).create();
                case 1:
                    return new Builder(getActivity()).setTitle(R.string.reset).setMessage(R.string.reset_message).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.dlg_ok, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            MyAlertDialogFragment.this.getOwner().resetToDefault();
                        }
                    }).create();
                default:
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("unknown id ");
                    stringBuilder.append(id);
                    throw new IllegalArgumentException(stringBuilder.toString());
            }
        }

        public void onCancel(DialogInterface dialog) {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mPicker = new ShortcutPickerHelper(getActivity(), this);
        this.mScreenOffGestureSharedPreferences = getActivity().getSharedPreferences(GESTURE_SETTINGS, 0);
        Resources settingsResources = null;
        try {
            settingsResources = getActivity().getPackageManager().getResourcesForApplication("com.android.settings");
            sFinalActionDialogArray = new FilteredDeviceFeaturesArray();
            sFinalActionDialogArray = DeviceUtils.filterUnsupportedDeviceFeatures(getActivity(), settingsResources.getStringArray(settingsResources.getIdentifier("com.android.settings:array/shortcut_action_screen_off_values", null, null)), settingsResources.getStringArray(settingsResources.getIdentifier("com.android.settings:array/shortcut_action_screen_off_entries", null, null)));
            reloadSettings();
            setHasOptionsMenu(true);
        } catch (Exception e) {
        }
    }

    private PreferenceScreen reloadSettings() {
        this.mCheckPreferences = false;
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }
        addPreferencesFromResource(R.xml.screen_off_gesture);
        prefs = getPreferenceScreen();
        this.mEnableGestures = (SwitchPreference) prefs.findPreference(PREF_GESTURE_ENABLE);
        this.mGestureC = prefs.findPreference(PREF_GESTURE_C);
        this.mGestureDoubleSwipe = prefs.findPreference(PREF_GESTURE_E);
        this.mGestureArrowUp = prefs.findPreference(PREF_GESTURE_W);
        this.mGestureArrowDown = prefs.findPreference(PREF_GESTURE_V);
        this.mGestureArrowLeft = prefs.findPreference(PREF_GESTURE_S);
        this.mGestureArrowRight = prefs.findPreference(PREF_GESTURE_Z);
        this.mGestureSwipeUp = prefs.findPreference(PREF_GESTURE_UP);
        this.mGestureSwipeDown = prefs.findPreference(PREF_GESTURE_DOWN);
        this.mGestureSwipeLeft = prefs.findPreference(PREF_GESTURE_LEFT);
        this.mGestureSwipeRight = prefs.findPreference(PREF_GESTURE_RIGHT);
        this.mGestureDoubleTap = prefs.findPreference(PREF_GESTURE_DOUBLE_TAP);
        setupOrUpdatePreference(this.mGestureC, this.mScreenOffGestureSharedPreferences.getString(PREF_GESTURE_C, ActionConstants.ACTION_CAMERA));
        setupOrUpdatePreference(this.mGestureDoubleSwipe, this.mScreenOffGestureSharedPreferences.getString(PREF_GESTURE_E, ActionConstants.ACTION_MEDIA_PLAY_PAUSE));
        setupOrUpdatePreference(this.mGestureArrowUp, this.mScreenOffGestureSharedPreferences.getString(PREF_GESTURE_W, ActionConstants.ACTION_TORCH));
        setupOrUpdatePreference(this.mGestureArrowDown, this.mScreenOffGestureSharedPreferences.getString(PREF_GESTURE_V, ActionConstants.ACTION_VIB_SILENT));
        setupOrUpdatePreference(this.mGestureArrowLeft, this.mScreenOffGestureSharedPreferences.getString(PREF_GESTURE_S, ActionConstants.ACTION_MEDIA_PREVIOUS));
        setupOrUpdatePreference(this.mGestureArrowRight, this.mScreenOffGestureSharedPreferences.getString(PREF_GESTURE_Z, ActionConstants.ACTION_MEDIA_NEXT));
        setupOrUpdatePreference(this.mGestureSwipeUp, this.mScreenOffGestureSharedPreferences.getString(PREF_GESTURE_UP, ActionConstants.ACTION_WAKE_DEVICE));
        setupOrUpdatePreference(this.mGestureSwipeDown, this.mScreenOffGestureSharedPreferences.getString(PREF_GESTURE_DOWN, ActionConstants.ACTION_VIB_SILENT));
        setupOrUpdatePreference(this.mGestureSwipeLeft, this.mScreenOffGestureSharedPreferences.getString(PREF_GESTURE_LEFT, ActionConstants.ACTION_MEDIA_PREVIOUS));
        setupOrUpdatePreference(this.mGestureSwipeRight, this.mScreenOffGestureSharedPreferences.getString(PREF_GESTURE_RIGHT, ActionConstants.ACTION_MEDIA_NEXT));
        setupOrUpdatePreference(this.mGestureDoubleTap, this.mScreenOffGestureSharedPreferences.getString(PREF_GESTURE_DOUBLE_TAP, ActionConstants.ACTION_WAKE_DEVICE));
        this.mEnableGestures.setChecked(this.mScreenOffGestureSharedPreferences.getBoolean(PREF_GESTURE_ENABLE, true));
        this.mEnableGestures.setOnPreferenceChangeListener(this);
        this.mCheckPreferences = true;
        return prefs;
    }

    private void setupOrUpdatePreference(Preference preference, String action) {
        if (preference != null) {
            if (action != null) {
                if (action.startsWith("**")) {
                    preference.setSummary(getDescription(action));
                } else {
                    preference.setSummary(AppHelper.getFriendlyNameForUri(getActivity(), getActivity().getPackageManager(), action));
                }
                preference.setOnPreferenceClickListener(this);
            }
        }
    }

    private String getDescription(String action) {
        if (sFinalActionDialogArray != null) {
            if (action != null) {
                int i = 0;
                for (String actionValue : sFinalActionDialogArray.values) {
                    if (action.equals(actionValue)) {
                        return sFinalActionDialogArray.entries[i];
                    }
                    i++;
                }
                return null;
            }
        }
        return null;
    }

    public boolean onPreferenceClick(Preference preference) {
        String settingsKey = null;
        int dialogTitle = 0;
        if (preference == this.mGestureDoubleTap) {
            settingsKey = PREF_GESTURE_DOUBLE_TAP;
            dialogTitle = R.string.gesture_double_tap_title;
        } else if (preference == this.mGestureC) {
            settingsKey = PREF_GESTURE_C;
            dialogTitle = R.string.gesture_c_title;
        } else if (preference == this.mGestureDoubleSwipe) {
            settingsKey = PREF_GESTURE_E;
            dialogTitle = R.string.gesture_e_title;
        } else if (preference == this.mGestureArrowUp) {
            settingsKey = PREF_GESTURE_W;
            dialogTitle = R.string.gesture_w_title;
        } else if (preference == this.mGestureArrowDown) {
            settingsKey = PREF_GESTURE_V;
            dialogTitle = R.string.gesture_v_title;
        } else if (preference == this.mGestureArrowLeft) {
            settingsKey = PREF_GESTURE_S;
            dialogTitle = R.string.gesture_s_title;
        } else if (preference == this.mGestureArrowRight) {
            settingsKey = PREF_GESTURE_Z;
            dialogTitle = R.string.gesture_z_title;
        } else if (preference == this.mGestureSwipeUp) {
            settingsKey = PREF_GESTURE_UP;
            dialogTitle = R.string.gesture_up_title;
        } else if (preference == this.mGestureSwipeDown) {
            settingsKey = PREF_GESTURE_DOWN;
            dialogTitle = R.string.gesture_down_title;
        } else if (preference == this.mGestureSwipeLeft) {
            settingsKey = PREF_GESTURE_LEFT;
            dialogTitle = R.string.gesture_left_title;
        } else if (preference == this.mGestureSwipeRight) {
            settingsKey = PREF_GESTURE_RIGHT;
            dialogTitle = R.string.gesture_right_title;
        }
        if (settingsKey == null) {
            return false;
        }
        showDialogInner(0, settingsKey, dialogTitle);
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!this.mCheckPreferences || preference != this.mEnableGestures) {
            return false;
        }
        this.mScreenOffGestureSharedPreferences.edit().putBoolean(PREF_GESTURE_ENABLE, ((Boolean) newValue).booleanValue()).commit();
        KernelControl.enableGestures(((Boolean) newValue).booleanValue());
        return true;
    }

    private void resetToDefault() {
        Editor editor = this.mScreenOffGestureSharedPreferences.edit();
        this.mScreenOffGestureSharedPreferences.edit().putBoolean(PREF_GESTURE_ENABLE, true).commit();
        editor.putString(PREF_GESTURE_C, ActionConstants.ACTION_CAMERA).commit();
        editor.putString(PREF_GESTURE_E, ActionConstants.ACTION_MEDIA_PLAY_PAUSE).commit();
        editor.putString(PREF_GESTURE_W, ActionConstants.ACTION_TORCH).commit();
        editor.putString(PREF_GESTURE_V, ActionConstants.ACTION_VIB_SILENT).commit();
        editor.putString(PREF_GESTURE_S, ActionConstants.ACTION_MEDIA_PREVIOUS).commit();
        editor.putString(PREF_GESTURE_Z, ActionConstants.ACTION_MEDIA_NEXT).commit();
        editor.putString(PREF_GESTURE_UP, ActionConstants.ACTION_WAKE_DEVICE).commit();
        editor.putString(PREF_GESTURE_DOWN, ActionConstants.ACTION_VIB_SILENT).commit();
        editor.putString(PREF_GESTURE_LEFT, ActionConstants.ACTION_MEDIA_PREVIOUS).commit();
        editor.putString(PREF_GESTURE_RIGHT, ActionConstants.ACTION_MEDIA_NEXT).commit();
        editor.putString(PREF_GESTURE_DOUBLE_TAP, ActionConstants.ACTION_WAKE_DEVICE).commit();
        editor.commit();
        KernelControl.enableGestures(true);
        reloadSettings();
    }

    public void onResume() {
        super.onResume();
    }

    public void shortcutPicked(String action, String description, Bitmap bmp, boolean isApplication) {
        if (this.mPendingSettingsKey != null) {
            if (action != null) {
                this.mScreenOffGestureSharedPreferences.edit().putString(this.mPendingSettingsKey, action).commit();
                reloadSettings();
                this.mPendingSettingsKey = null;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != -1) {
            this.mPendingSettingsKey = null;
        } else if (requestCode == 100 || requestCode == 101 || requestCode == 102) {
            this.mPicker.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            showDialogInner(1, null, 0);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 1, 0, R.string.reset).setIcon(R.drawable.ic_settings_reset).setShowAsAction(2);
    }

    private void showDialogInner(int id, String settingsKey, int dialogTitle) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id, settingsKey, dialogTitle);
        newFragment.setTargetFragment(this, 0);
        FragmentManager fragmentManager = getFragmentManager();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("dialog ");
        stringBuilder.append(id);
        newFragment.show(fragmentManager, stringBuilder.toString());
    }
}
