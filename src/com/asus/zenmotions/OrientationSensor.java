package com.asus.zenmotions;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationSensor implements SensorEventListener {
    private static final float MATH_PI_1_4 = 0.785398f;
    private static final float MATH_PI_3_4 = 2.35619f;
    private static final int ORIENTATION_DELAY = 60000;
    public static final int ORIENTATION_FACE_DOWN = 1;
    public static final int ORIENTATION_FACE_UP = 2;
    private static final int ORIENTATION_LATENCY = 0;
    public static final int ORIENTATION_UNKNOWN = 0;
    public static final int ORIENTATION_VERTICAL = 3;
    private Sensor mAccelerometerSensor;
    private boolean mEnabled = false;
    private float[] mGravity;
    private float[] mMagnetic;
    private Sensor mMagneticFieldSensor;
    private OrientationListener mOrientationListener;
    private boolean mReady;
    private SensorManager mSensorManager;
    private int mState;

    public interface OrientationListener {
        void onEvent();
    }

    public boolean isFaceDown() {
        return this.mReady && this.mState == 1;
    }

    public boolean isFaceUp() {
        return this.mReady && this.mState == 2;
    }

    public boolean isVertical() {
        return this.mReady && this.mState == 3;
    }

    public OrientationSensor(Context context, SensorManager sensorManager, OrientationListener orientationListener) {
        reset();
        this.mAccelerometerSensor = sensorManager.getDefaultSensor(1, false);
        this.mMagneticFieldSensor = sensorManager.getDefaultSensor(2, false);
        this.mOrientationListener = orientationListener;
        this.mSensorManager = sensorManager;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.values.length != 0) {
            switch (event.sensor.getType()) {
                case 1:
                    this.mGravity = event.values;
                    break;
                case 2:
                    this.mMagnetic = event.values;
                    break;
                default:
                    break;
            }
            if (!(this.mGravity == null || this.mMagnetic == null)) {
                float[] rotationMatrix = new float[9];
                if (SensorManager.getRotationMatrix(rotationMatrix, new float[9], this.mGravity, this.mMagnetic)) {
                    float[] values = new float[3];
                    this.mState = 0;
                    SensorManager.getOrientation(rotationMatrix, values);
                    if (values[1] > -0.785398f && values[1] < MATH_PI_1_4) {
                        if (values[2] > -0.785398f && values[2] < MATH_PI_1_4) {
                            this.mState = 2;
                        } else if (values[2] < -2.35619f || values[2] > MATH_PI_3_4) {
                            this.mState = 1;
                        }
                    }
                    if (values[1] < -0.785398f || values[1] > MATH_PI_1_4 || ((values[2] > MATH_PI_1_4 && values[2] < MATH_PI_3_4) || (values[2] > -2.35619f && values[2] < -0.785398f))) {
                        this.mState = 3;
                    }
                    this.mReady = true;
                    this.mOrientationListener.onEvent();
                }
            }
        }
    }

    public void enable() {
        if (!this.mEnabled && this.mAccelerometerSensor != null && this.mMagneticFieldSensor != null) {
            reset();
            this.mState = 0;
            this.mSensorManager.registerListener(this, this.mAccelerometerSensor, ORIENTATION_DELAY, 0);
            this.mSensorManager.registerListener(this, this.mMagneticFieldSensor, ORIENTATION_DELAY, 0);
            this.mEnabled = true;
        }
    }

    public void reset() {
        this.mGravity = null;
        this.mMagnetic = null;
        this.mReady = false;
    }

    public void disable() {
        if (this.mEnabled && this.mAccelerometerSensor != null && this.mMagneticFieldSensor != null) {
            this.mSensorManager.unregisterListener(this, this.mAccelerometerSensor);
            this.mSensorManager.unregisterListener(this, this.mMagneticFieldSensor);
            this.mEnabled = false;
        }
    }
}
