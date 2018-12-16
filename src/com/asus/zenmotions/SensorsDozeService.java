package com.asus.zenmotions;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import com.asus.zenmotions.OrientationSensor.OrientationListener;
import com.asus.zenmotions.PickUpSensor.PickUpListener;
import com.asus.zenmotions.ProximitySensor.ProximityListener;

public class SensorsDozeService extends Service {
    public static final boolean DEBUG = false;
    private static final String DOZE_INTENT = "com.android.systemui.doze.pulse";
    private static final int HANDWAVE_DELTA_NS = 1000000000;
    private static final String KEY_GESTURE_HAND_WAVE = "gesture_hand_wave";
    private static final String KEY_GESTURE_PICK_UP = "gesture_pick_up";
    private static final String KEY_GESTURE_POCKET = "gesture_pocket";
    private static final String KEY_PROXIMITY_WAKE = "proximity_wake_enable";
    private static final int PULSE_MIN_INTERVAL_MS = 5000;
    private static final int SENSORS_WAKELOCK_DURATION = 1000;
    public static final String TAG = "SensorsDozeService";
    private static final int VIBRATOR_ACKNOWLEDGE = 40;
    private Context mContext;
    private boolean mDozeEnabled = false;
    private boolean mHandwaveDoze = false;
    private boolean mHandwaveGestureEnabled = false;
    private long mLastPulseTimestamp = 0;
    private long mLastStowedTimestamp = 0;
    private OrientationListener mOrientationListener = new OrientationListener() {
        public void onEvent() {
            SensorsDozeService.this.setOrientationSensor(false, false);
            SensorsDozeService.this.handleOrientation();
        }
    };
    private OrientationSensor mOrientationSensor;
    private boolean mPickUpDoze = false;
    private boolean mPickUpGestureEnabled = false;
    private PickUpListener mPickUpListener = new PickUpListener() {
        public void onEvent() {
            SensorsDozeService.this.mPickUpState = SensorsDozeService.this.mPickUpSensor.isPickedUp();
            SensorsDozeService.this.handlePickUp();
        }

        public void onInit() {
            SensorsDozeService.this.mPickUpState = SensorsDozeService.this.mPickUpSensor.isPickedUp();
        }
    };
    private PickUpSensor mPickUpSensor;
    private boolean mPickUpState = false;
    private boolean mPocketDoze = false;
    private boolean mPocketGestureEnabled = false;
    private PowerManager mPowerManager;
    private OnSharedPreferenceChangeListener mPrefListener = new OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (SensorsDozeService.KEY_GESTURE_HAND_WAVE.equals(key)) {
                SensorsDozeService.this.mHandwaveGestureEnabled = sharedPreferences.getBoolean(SensorsDozeService.KEY_GESTURE_HAND_WAVE, false);
            } else if (SensorsDozeService.KEY_GESTURE_PICK_UP.equals(key)) {
                SensorsDozeService.this.mPickUpGestureEnabled = sharedPreferences.getBoolean(SensorsDozeService.KEY_GESTURE_PICK_UP, false);
            } else if (SensorsDozeService.KEY_GESTURE_POCKET.equals(key)) {
                SensorsDozeService.this.mPocketGestureEnabled = sharedPreferences.getBoolean(SensorsDozeService.KEY_GESTURE_POCKET, false);
            } else if (SensorsDozeService.KEY_PROXIMITY_WAKE.equals(key)) {
                SensorsDozeService.this.mProximityWakeEnabled = sharedPreferences.getBoolean(SensorsDozeService.KEY_PROXIMITY_WAKE, false);
            }
        }
    };
    private ProximityListener mProximityListener = new ProximityListener() {
        public void onEvent(boolean isNear, long timestamp) {
            SensorsDozeService.this.mProximityNear = isNear;
            SensorsDozeService.this.handleProximity(timestamp);
        }

        public void onInit(boolean isNear, long timestamp) {
            SensorsDozeService.this.mLastStowedTimestamp = timestamp;
            SensorsDozeService.this.mProximityNear = isNear;
            if (!SensorsDozeService.this.isEventPending() && !isNear && SensorsDozeService.this.isPickUpEnabled()) {
                SensorsDozeService.this.setPickUpSensor(true, false);
            }
        }
    };
    private boolean mProximityNear = false;
    private ProximitySensor mProximitySensor;
    private boolean mProximityWake = false;
    private boolean mProximityWakeEnabled = false;
    private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                SensorsDozeService.this.onDisplayOff();
            } else if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                SensorsDozeService.this.onDisplayOn();
            }
        }
    };
    private SensorManager mSensorManager;
    private WakeLock mSensorsWakeLock;

    public void onCreate() {
        super.onCreate();
        this.mContext = this;
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mSensorsWakeLock = this.mPowerManager.newWakeLock(1, "SensorsDozeServiceWakeLock");
        this.mOrientationSensor = new OrientationSensor(this.mContext, this.mSensorManager, this.mOrientationListener);
        this.mPickUpSensor = new PickUpSensor(this.mContext, this.mSensorManager, this.mPickUpListener);
        this.mProximitySensor = new ProximitySensor(this.mContext, this.mSensorManager, this.mProximityListener);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        loadPreferences(sharedPrefs);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this.mPrefListener);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentScreen = new IntentFilter("android.intent.action.SCREEN_ON");
        intentScreen.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mScreenStateReceiver, intentScreen);
        if (!this.mPowerManager.isInteractive()) {
            onDisplayOff();
        }
        return 1;
    }

    public void onDestroy() {
        super.onDestroy();
        setOrientationSensor(false, true);
        setPickUpSensor(false, true);
        setProximitySensor(false, true);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getDozeEnabled() {
        boolean enabled = true;
        if (Secure.getInt(this.mContext.getContentResolver(), "doze_enabled", 1) == 0) {
            enabled = false;
        }
        this.mDozeEnabled = enabled;
    }

    private boolean isDozeEnabled() {
        return this.mDozeEnabled;
    }

    private boolean isHandwaveEnabled() {
        return this.mHandwaveGestureEnabled && isDozeEnabled();
    }

    private boolean isPickUpEnabled() {
        return this.mPickUpGestureEnabled && isDozeEnabled();
    }

    private boolean isPocketEnabled() {
        return this.mPocketGestureEnabled && isDozeEnabled();
    }

    private boolean isProximityWakeEnabled() {
        return this.mProximityWakeEnabled;
    }

    private boolean isEventPending() {
        if (!(this.mHandwaveDoze || this.mPickUpDoze || this.mPocketDoze)) {
            if (!this.mProximityWake) {
                return false;
            }
        }
        return true;
    }

    private void handleProximity(long timestamp) {
        boolean quickWave = timestamp - this.mLastStowedTimestamp < 1000000000;
        getDozeEnabled();
        if (this.mProximityNear) {
            this.mLastStowedTimestamp = timestamp;
            setOrientationSensor(false, false);
            setPickUpSensor(false, false);
            return;
        }
        this.mHandwaveDoze = false;
        this.mPickUpDoze = false;
        this.mPocketDoze = false;
        this.mProximityWake = false;
        if (isHandwaveEnabled() && isPickUpEnabled() && isPocketEnabled()) {
            this.mHandwaveDoze = quickWave;
            this.mPickUpDoze = !quickWave;
            this.mPocketDoze = !quickWave;
            setOrientationSensor(true, false);
        } else if (isProximityWakeEnabled() && quickWave) {
            this.mProximityWake = true;
            setOrientationSensor(true, false);
        } else if (isHandwaveEnabled() && quickWave) {
            this.mHandwaveDoze = true;
            setOrientationSensor(true, false);
        } else if ((isPickUpEnabled() || isPocketEnabled()) && !quickWave) {
            this.mPickUpDoze = isPickUpEnabled();
            this.mPocketDoze = isPocketEnabled();
            setOrientationSensor(true, false);
        } else if (isPickUpEnabled()) {
            setPickUpSensor(true, false);
        }
    }

    private void handleOrientation() {
        if (!this.mProximityNear) {
            analyseDoze();
        }
    }

    private void handlePickUp() {
        getDozeEnabled();
        if (this.mPickUpState && isPickUpEnabled()) {
            this.mPickUpDoze = true;
            launchWakeLock();
            analyseDoze();
            return;
        }
        this.mPickUpDoze = false;
    }

    private void analyseDoze() {
        getDozeEnabled();
        if (this.mHandwaveDoze && !this.mOrientationSensor.isFaceDown()) {
            launchDozePulse();
        } else if (this.mPickUpDoze && ((this.mPickUpState && !this.mProximityNear) || (!this.mPickUpState && this.mOrientationSensor.isFaceDown()))) {
            launchDozePulse();
        } else if (this.mPocketDoze && this.mOrientationSensor.isVertical()) {
            launchDozePulse();
        } else if (this.mProximityWake && !this.mOrientationSensor.isFaceDown()) {
            launchDeviceWake();
        }
        if (!this.mProximityNear && isPickUpEnabled()) {
            setPickUpSensor(true, false);
        }
        resetValues();
    }

    private void launchDozePulse() {
        long delta;
        if (this.mLastPulseTimestamp != 0) {
            delta = SystemClock.elapsedRealtime() - this.mLastPulseTimestamp;
        } else {
            delta = 5000;
        }
        if (delta >= 5000) {
            launchWakeLock();
            launchAcknowledge();
            this.mLastPulseTimestamp = SystemClock.elapsedRealtime();
            this.mContext.sendBroadcastAsUser(new Intent(DOZE_INTENT), UserHandle.ALL);
        }
    }

    private void launchDeviceWake() {
        this.mSensorsWakeLock.acquire(1000);
        launchAcknowledge();
        this.mPowerManager.wakeUp(SystemClock.uptimeMillis());
    }

    private void launchWakeLock() {
        this.mSensorsWakeLock.acquire(1000);
    }

    private void launchAcknowledge() {
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        boolean z = true;
        if (SettingsUtils.getIntSystem(this.mContext, this.mContext.getContentResolver(), SettingsUtils.TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK, 1) == 0) {
            z = false;
        }
        boolean enabled = z;
        if (audioManager.getRingerMode() == 0) {
            return;
        }
        if (enabled) {
            vibrator.vibrate(40);
        }
    }

    private void resetValues() {
        this.mHandwaveDoze = false;
        this.mPickUpDoze = false;
        this.mPocketDoze = false;
        this.mProximityWake = false;
    }

    private void setOrientationSensor(boolean enabled, boolean reset) {
        if (this.mOrientationSensor != null) {
            if (reset) {
                this.mOrientationSensor.reset();
            }
            if (enabled) {
                setPickUpSensor(false, false);
                launchWakeLock();
                this.mOrientationSensor.enable();
            } else {
                this.mOrientationSensor.disable();
            }
        }
    }

    private void setPickUpSensor(boolean enabled, boolean reset) {
        if (this.mPickUpSensor != null) {
            if (reset) {
                this.mPickUpSensor.reset();
            }
            if (enabled) {
                setOrientationSensor(false, false);
                this.mPickUpSensor.enable();
            } else {
                this.mPickUpSensor.disable();
            }
        }
    }

    private void setProximitySensor(boolean enabled, boolean reset) {
        if (this.mProximitySensor != null) {
            if (reset) {
                this.mProximitySensor.reset();
            }
            if (enabled) {
                this.mProximitySensor.enable();
            } else {
                this.mProximitySensor.disable();
            }
        }
    }

    private void onDisplayOn() {
        setOrientationSensor(false, true);
        setPickUpSensor(false, true);
        setProximitySensor(false, true);
    }

    private void onDisplayOff() {
        getDozeEnabled();
        this.mLastPulseTimestamp = 0;
        if (isHandwaveEnabled() || isPickUpEnabled() || isPocketEnabled() || isProximityWakeEnabled()) {
            resetValues();
            setOrientationSensor(false, true);
            setPickUpSensor(false, true);
            setProximitySensor(true, true);
        }
    }

    private void loadPreferences(SharedPreferences sharedPreferences) {
        this.mHandwaveGestureEnabled = sharedPreferences.getBoolean(KEY_GESTURE_HAND_WAVE, false);
        this.mPickUpGestureEnabled = sharedPreferences.getBoolean(KEY_GESTURE_PICK_UP, false);
        this.mPocketGestureEnabled = sharedPreferences.getBoolean(KEY_GESTURE_POCKET, false);
        this.mProximityWakeEnabled = sharedPreferences.getBoolean(KEY_PROXIMITY_WAKE, false);
    }
}
