package com.asus.zenmotions;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class PickUpSensor implements SensorEventListener {
    private static final int PICKUP_DELAY = 500000;
    private static final int PICKUP_LATENCY = 100000;
    public static final int PICK_UP_FALSE = 1;
    private static final float PICK_UP_SAFEZONE = 5.0f;
    private static final float PICK_UP_THRESHOLD = 6.0f;
    public static final int PICK_UP_TRUE = 2;
    public static final int PICK_UP_UNKNOWN = 0;
    private boolean mEnabled = false;
    private PickUpListener mPickUpListener;
    private Sensor mPickUpSensor;
    private boolean mReady;
    private SensorManager mSensorManager;
    private int mState;

    public interface PickUpListener {
        void onEvent();

        void onInit();
    }

    public boolean isPickedUp() {
        return this.mReady && this.mState == 2;
    }

    public PickUpSensor(Context context, SensorManager sensorManager, PickUpListener pickUpListener) {
        reset();
        this.mPickUpSensor = sensorManager.getDefaultSensor(1, false);
        this.mPickUpListener = pickUpListener;
        this.mSensorManager = sensorManager;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.values.length != 0) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            if (isPickUpAbove(x, y, PICK_UP_SAFEZONE)) {
                if (isPickUpAbove(x, y, PICK_UP_THRESHOLD) && this.mState != 2) {
                    this.mState = 2;
                    if (this.mReady) {
                        this.mPickUpListener.onEvent();
                    }
                }
            } else if (this.mState != 1) {
                this.mState = 1;
                this.mState = 1;
                if (this.mReady) {
                    this.mPickUpListener.onEvent();
                }
            }
            if (!this.mReady) {
                this.mReady = true;
                this.mPickUpListener.onInit();
            }
        }
    }

    public boolean isPickUpAbove(float x, float y, float threshold) {
        if (x >= (-threshold) && x <= threshold) {
            if (y <= threshold) {
                return false;
            }
        }
        return true;
    }

    public void enable() {
        if (!this.mEnabled && this.mPickUpSensor != null) {
            reset();
            this.mSensorManager.registerListener(this, this.mPickUpSensor, PICKUP_DELAY, PICKUP_LATENCY);
            this.mEnabled = true;
        }
    }

    public void reset() {
        this.mReady = false;
        this.mState = 0;
    }

    public void disable() {
        if (this.mEnabled && this.mPickUpSensor != null) {
            this.mSensorManager.unregisterListener(this, this.mPickUpSensor);
            this.mEnabled = false;
        }
    }
}
