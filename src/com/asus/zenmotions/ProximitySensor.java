package com.asus.zenmotions;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ProximitySensor implements SensorEventListener {
    private static final int PROXIMITY_DELAY = 1000000;
    private static final int PROXIMITY_LATENCY = 100000;
    private boolean mEnabled = false;
    private float mMaxRange;
    private ProximityListener mProximityListener;
    private Sensor mProximitySensor;
    private boolean mReady;
    private SensorManager mSensorManager;
    private boolean mState;

    public interface ProximityListener {
        void onEvent(boolean z, long j);

        void onInit(boolean z, long j);
    }

    public ProximitySensor(Context context, SensorManager sensorManager, ProximityListener proximitylistener) {
        reset();
        this.mProximitySensor = sensorManager.getDefaultSensor(8, true);
        this.mProximityListener = proximitylistener;
        this.mSensorManager = sensorManager;
        if (this.mProximitySensor != null) {
            this.mMaxRange = this.mProximitySensor.getMaximumRange();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.values.length != 0) {
            boolean z = false;
            if (event.values[0] < this.mMaxRange) {
                z = true;
            }
            boolean isNear = z;
            if (this.mState != isNear) {
                this.mState = isNear;
                if (this.mReady) {
                    this.mProximityListener.onEvent(this.mState, event.timestamp);
                }
            }
            if (!this.mReady) {
                this.mProximityListener.onInit(this.mState, event.timestamp);
                this.mReady = true;
            }
        }
    }

    public void enable() {
        if (!this.mEnabled && this.mProximitySensor != null) {
            this.mSensorManager.registerListener(this, this.mProximitySensor, PROXIMITY_DELAY, PROXIMITY_LATENCY);
            this.mEnabled = true;
        }
    }

    public void reset() {
        this.mReady = false;
        this.mState = false;
    }

    public void disable() {
        if (this.mEnabled && this.mProximitySensor != null) {
            this.mSensorManager.unregisterListener(this, this.mProximitySensor);
            this.mEnabled = false;
        }
    }
}
