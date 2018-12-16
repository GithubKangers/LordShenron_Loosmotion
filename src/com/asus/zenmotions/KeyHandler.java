package com.asus.zenmotions;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.provider.Settings.System;
import android.util.Log;
import android.view.KeyEvent;
import com.android.internal.os.AlternativeDeviceKeyHandler;
import com.android.internal.util.ArrayUtils;
import com.asus.zenmotions.settings.ScreenOffGesture;
import com.asus.zenmotions.util.Action;
import com.asus.zenmotions.util.ActionConstants;

public class KeyHandler implements AlternativeDeviceKeyHandler {
    private static final boolean DEBUG = true;
    private static final String FPC_CONTROL_PATH = "/sys/devices/soc/soc:fpc_fpc1020/proximity_state";
    private static final int GESTURE_C_SCANCODE = 249;
    private static final int GESTURE_DOUBLE_TAP = 260;
    private static final int GESTURE_E_SCANCODE = 250;
    private static final int GESTURE_REQUEST = 1;
    private static final int GESTURE_SWIPE_DOWN = 256;
    private static final int GESTURE_SWIPE_LEFT = 257;
    private static final int GESTURE_SWIPE_RIGHT = 258;
    private static final int GESTURE_SWIPE_UP = 255;
    private static final int GESTURE_S_SCANCODE = 251;
    private static final int GESTURE_V_SCANCODE = 252;
    private static final int GESTURE_WAKELOCK_DURATION = 2000;
    private static final int GESTURE_W_SCANCODE = 253;
    private static final int GESTURE_Z_SCANCODE = 254;
    private static final int KEYCODE_SLIDER_BOTTOM = 603;
    private static final int KEYCODE_SLIDER_MIDDLE = 602;
    private static final int KEYCODE_SLIDER_TOP = 601;
    private static final String KEY_CONTROL_PATH = "/proc/s1302/virtual_key";
    private static final int KEY_DOUBLE_TAP = 143;
    private static final String TAG = KeyHandler.class.getSimpleName();
    private static final int[] sHandledGestures = new int[]{KEYCODE_SLIDER_TOP, KEYCODE_SLIDER_MIDDLE, KEYCODE_SLIDER_BOTTOM};
    private static final int[] sProxiCheckedGestures = new int[]{GESTURE_C_SCANCODE, 250, GESTURE_V_SCANCODE, GESTURE_W_SCANCODE, GESTURE_S_SCANCODE, GESTURE_Z_SCANCODE, 255, 256, 257, GESTURE_SWIPE_RIGHT, GESTURE_DOUBLE_TAP, 143};
    private static final int[] sSupportedGestures = new int[]{GESTURE_C_SCANCODE, 250, GESTURE_V_SCANCODE, GESTURE_W_SCANCODE, GESTURE_S_SCANCODE, GESTURE_Z_SCANCODE, 255, 256, 257, GESTURE_SWIPE_RIGHT, GESTURE_DOUBLE_TAP, KEYCODE_SLIDER_TOP, KEYCODE_SLIDER_MIDDLE, KEYCODE_SLIDER_BOTTOM};
    private final AudioManager mAudioManager;
    private final Context mContext;
    private int mCurrentPosition;
    private EventHandler mEventHandler;
    private Context mGestureContext = null;
    private WakeLock mGestureWakeLock;
    private Handler mHandler;
    private final NotificationManager mNoMan;
    private final PowerManager mPowerManager;
    private SensorEventListener mProximitySensor = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            KeyHandler keyHandler = KeyHandler.this;
            boolean z = false;
            if (event.values[0] < KeyHandler.this.mSensor.getMaximumRange()) {
                z = true;
            }
            keyHandler.mProxyIsNear = z;
            String access$800 = KeyHandler.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mProxyIsNear = ");
            stringBuilder.append(KeyHandler.this.mProxyIsNear);
            Log.d(access$800, stringBuilder.toString());
            if (Utils.fileWritable(KeyHandler.FPC_CONTROL_PATH)) {
                Utils.writeValue(KeyHandler.FPC_CONTROL_PATH, KeyHandler.this.mProxyIsNear ? "1" : "0");
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    WakeLock mProximityWakeLock;
    private boolean mProxyIsNear;
    private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                KeyHandler.this.onDisplayOn();
            } else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                KeyHandler.this.onDisplayOff();
            }
        }
    };
    private Sensor mSensor;
    private SensorManager mSensorManager;
    private SettingsObserver mSettingsObserver;
    private boolean mUseProxiCheck;
    private Vibrator mVibrator;

    private class EventHandler extends Handler {
        private EventHandler() {
        }

        /* synthetic */ EventHandler(KeyHandler x0, AnonymousClass1 x1) {
            this();
        }

        public void handleMessage(Message msg) {
            String action = null;
            switch (msg.obj.getScanCode()) {
                case KeyHandler.GESTURE_C_SCANCODE /*249*/:
                    action = KeyHandler.this.getGestureSharedPreferences().getString(ScreenOffGesture.PREF_GESTURE_C, ActionConstants.ACTION_CAMERA);
                    KeyHandler.this.doHapticFeedback();
                    break;
                case 250:
                    action = KeyHandler.this.getGestureSharedPreferences().getString(ScreenOffGesture.PREF_GESTURE_E, ActionConstants.ACTION_MEDIA_PLAY_PAUSE);
                    KeyHandler.this.doHapticFeedback();
                    break;
                case KeyHandler.GESTURE_S_SCANCODE /*251*/:
                    action = KeyHandler.this.getGestureSharedPreferences().getString(ScreenOffGesture.PREF_GESTURE_S, ActionConstants.ACTION_MEDIA_PREVIOUS);
                    KeyHandler.this.doHapticFeedback();
                    break;
                case KeyHandler.GESTURE_V_SCANCODE /*252*/:
                    action = KeyHandler.this.getGestureSharedPreferences().getString(ScreenOffGesture.PREF_GESTURE_V, ActionConstants.ACTION_VIB_SILENT);
                    KeyHandler.this.doHapticFeedback();
                    break;
                case KeyHandler.GESTURE_W_SCANCODE /*253*/:
                    action = KeyHandler.this.getGestureSharedPreferences().getString(ScreenOffGesture.PREF_GESTURE_W, ActionConstants.ACTION_TORCH);
                    KeyHandler.this.doHapticFeedback();
                    break;
                case KeyHandler.GESTURE_Z_SCANCODE /*254*/:
                    action = KeyHandler.this.getGestureSharedPreferences().getString(ScreenOffGesture.PREF_GESTURE_Z, ActionConstants.ACTION_MEDIA_NEXT);
                    KeyHandler.this.doHapticFeedback();
                    break;
                case 255:
                    action = KeyHandler.this.getGestureSharedPreferences().getString(ScreenOffGesture.PREF_GESTURE_UP, ActionConstants.ACTION_WAKE_DEVICE);
                    KeyHandler.this.doHapticFeedback();
                    break;
                case 256:
                    action = KeyHandler.this.getGestureSharedPreferences().getString(ScreenOffGesture.PREF_GESTURE_DOWN, ActionConstants.ACTION_VIB_SILENT);
                    KeyHandler.this.doHapticFeedback();
                    break;
                case 257:
                    action = KeyHandler.this.getGestureSharedPreferences().getString(ScreenOffGesture.PREF_GESTURE_LEFT, ActionConstants.ACTION_MEDIA_PREVIOUS);
                    KeyHandler.this.doHapticFeedback();
                    break;
                case KeyHandler.GESTURE_SWIPE_RIGHT /*258*/:
                    action = KeyHandler.this.getGestureSharedPreferences().getString(ScreenOffGesture.PREF_GESTURE_RIGHT, ActionConstants.ACTION_MEDIA_NEXT);
                    KeyHandler.this.doHapticFeedback();
                    break;
                case KeyHandler.GESTURE_DOUBLE_TAP /*260*/:
                    action = KeyHandler.this.getGestureSharedPreferences().getString(ScreenOffGesture.PREF_GESTURE_DOUBLE_TAP, ActionConstants.ACTION_WAKE_DEVICE);
                    KeyHandler.this.doHapticFeedback();
                    break;
                default:
                    break;
            }
            if (action != null) {
                if (action == null || !action.equals(ActionConstants.ACTION_NULL)) {
                    if (action.equals(ActionConstants.ACTION_CAMERA) || !action.startsWith("**")) {
                        Action.processAction(KeyHandler.this.mContext, ActionConstants.ACTION_WAKE_DEVICE, false);
                    }
                    Action.processAction(KeyHandler.this.mContext, action, false);
                }
            }
        }
    }

    private class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            KeyHandler.this.mContext.getContentResolver().registerContentObserver(System.getUriFor("device_proxi_check_enabled"), false, this);
            update();
        }

        public void onChange(boolean selfChange) {
            update();
        }

        public void update() {
            KeyHandler keyHandler = KeyHandler.this;
            boolean z = true;
            if (System.getIntForUser(KeyHandler.this.mContext.getContentResolver(), "device_proxi_check_enabled", 1, -2) != 1) {
                z = false;
            }
            keyHandler.mUseProxiCheck = z;
        }
    }

    private Intent createIntent(String value) {
        ComponentName componentName = ComponentName.unflattenFromString(value);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setFlags(270532608);
        intent.setComponent(componentName);
        return intent;
    }

    public KeyHandler(Context context) {
        this.mContext = context;
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mSettingsObserver.observe();
        this.mEventHandler = new EventHandler(this, null);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mNoMan = (NotificationManager) context.getSystemService("notification");
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mSensor = this.mSensorManager.getDefaultSensor(8);
        IntentFilter screenStateFilter = new IntentFilter("android.intent.action.SCREEN_ON");
        screenStateFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mScreenStateReceiver, screenStateFilter);
        this.mProximityWakeLock = this.mPowerManager.newWakeLock(1, "ProximityWakeLock");
        this.mHandler = new Handler();
        this.mGestureWakeLock = this.mPowerManager.newWakeLock(1, "GestureWakeLock");
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        if (this.mVibrator == null || !this.mVibrator.hasVibrator()) {
            this.mVibrator = null;
        }
        try {
            this.mGestureContext = this.mContext.createPackageContext("com.asus.zenmotions", 2);
        } catch (NameNotFoundException e) {
        }
    }

    private void onDisplayOn() {
        if (this.mUseProxiCheck) {
            Log.d(TAG, "Display on");
            this.mSensorManager.unregisterListener(this.mProximitySensor, this.mSensor);
        }
    }

    private void onDisplayOff() {
        if (this.mUseProxiCheck) {
            Log.d(TAG, "Display off");
            this.mSensorManager.registerListener(this.mProximitySensor, this.mSensor, 3);
        }
    }

    private void doHapticFeedback() {
        if (this.mVibrator != null) {
            this.mVibrator.vibrate(50);
        }
    }

    private SharedPreferences getGestureSharedPreferences() {
        return this.mGestureContext.getSharedPreferences(ScreenOffGesture.GESTURE_SETTINGS, 4);
    }

    public boolean handleKeyEvent(KeyEvent event) {
        if (event.getAction() != 1) {
            return false;
        }
        int scanCode = event.getScanCode();
        boolean isKeySupported = ArrayUtils.contains(sSupportedGestures, scanCode);
        boolean isSliderModeSupported = ArrayUtils.contains(sHandledGestures, event.getScanCode());
        if (isKeySupported && !this.mEventHandler.hasMessages(1)) {
            Message msg = getMessageForKeyEvent(event);
            if (scanCode < KEYCODE_SLIDER_TOP && this.mProximitySensor != null) {
                this.mEventHandler.sendMessageDelayed(msg, 200);
            } else if (isSliderModeSupported) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("scanCode=");
                stringBuilder.append(event.getScanCode());
                Log.i(str, stringBuilder.toString());
                switch (event.getScanCode()) {
                    case KEYCODE_SLIDER_TOP /*601*/:
                        this.mCurrentPosition = KEYCODE_SLIDER_TOP;
                        Log.i(TAG, "KEYCODE_SLIDER_TOP");
                        this.mHandler.postDelayed(new Runnable() {
                            public void run() {
                                if (KeyHandler.this.mCurrentPosition == KeyHandler.KEYCODE_SLIDER_TOP) {
                                }
                            }
                        }, 250);
                        return true;
                    case KEYCODE_SLIDER_MIDDLE /*602*/:
                        this.mCurrentPosition = KEYCODE_SLIDER_MIDDLE;
                        Log.i(TAG, "KEYCODE_SLIDER_MIDDLE");
                        this.mHandler.postDelayed(new Runnable() {
                            public void run() {
                                if (KeyHandler.this.mCurrentPosition == KeyHandler.KEYCODE_SLIDER_MIDDLE) {
                                }
                            }
                        }, 50);
                        return true;
                    case KEYCODE_SLIDER_BOTTOM /*603*/:
                        this.mCurrentPosition = KEYCODE_SLIDER_BOTTOM;
                        Log.i(TAG, "KEYCODE_SLIDER_BOTTOM");
                        this.mHandler.postDelayed(new Runnable() {
                            public void run() {
                                if (KeyHandler.this.mCurrentPosition == KeyHandler.KEYCODE_SLIDER_BOTTOM) {
                                }
                            }
                        }, 50);
                        return true;
                    default:
                        this.mEventHandler.removeMessages(1);
                        this.mEventHandler.sendMessage(msg);
                        break;
                }
            } else {
                this.mEventHandler.sendMessage(msg);
            }
        }
        return isKeySupported;
    }

    private Message getMessageForKeyEvent(KeyEvent keyEvent) {
        Message msg = this.mEventHandler.obtainMessage(1);
        msg.obj = keyEvent;
        return msg;
    }

    public boolean isDisabledKeyEvent(KeyEvent event) {
        boolean isProxyCheckRequired = this.mUseProxiCheck && ArrayUtils.contains(sProxiCheckedGestures, event.getScanCode());
        if (!this.mProxyIsNear || !isProxyCheckRequired) {
            return false;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("isDisabledKeyEvent: blocked by proxi sensor - scanCode=");
        stringBuilder.append(event.getScanCode());
        Log.i(str, stringBuilder.toString());
        return true;
    }

    public boolean isWakeEvent(KeyEvent event) {
        boolean z = false;
        if (event.getAction() != 1) {
            return false;
        }
        if (event.getScanCode() == 143) {
            z = true;
        }
        return z;
    }

    public boolean isCameraLaunchEvent(KeyEvent event) {
        boolean z = false;
        if (event.getAction() != 1) {
            return false;
        }
        if (event.getScanCode() == GESTURE_C_SCANCODE) {
            z = true;
        }
        return z;
    }

    public boolean canHandleKeyEvent(KeyEvent event) {
        return ArrayUtils.contains(sSupportedGestures, event.getScanCode());
    }

    public Intent isActivityLaunchEvent(KeyEvent event) {
        return event.getAction() != 1 ? null : null;
    }
}
