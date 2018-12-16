package com.asus.zenmotions.kcal.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.asus.zenmotions.R;
import com.asus.zenmotions.kcal.Utils;

public class SeekBarPreference extends Preference {
    final int UPDATE = 0;
    private OnPreferenceChangeListener changer;
    int currentValue = this.def;
    public int def = 256;
    public int interval = 1;
    public int maximum = 256;
    public int minimum = 1;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, 0, 0);
        this.minimum = typedArray.getInt(6, this.minimum);
        this.maximum = typedArray.getInt(4, this.maximum);
        this.def = typedArray.getInt(3, this.def);
        typedArray.recycle();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    private void bind(View layout) {
        final EditText monitorBox = (EditText) layout.findViewById(R.id.monitor_box);
        final SeekBar bar = (SeekBar) layout.findViewById(R.id.seek_bar);
        monitorBox.setInputType(2);
        bar.setMax(this.maximum - this.minimum);
        bar.setProgress(this.currentValue - this.minimum);
        monitorBox.setText(String.valueOf(this.currentValue));
        monitorBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    monitorBox.setSelection(monitorBox.getText().length());
                }
            }
        });
        monitorBox.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
                if (keyCode != 6) {
                    return false;
                }
                v.clearFocus();
                ((InputMethodManager) SeekBarPreference.this.getContext().getSystemService("input_method")).hideSoftInputFromWindow(v.getWindowToken(), 0);
                SeekBarPreference.this.currentValue = (int) Utils.clamp((double) Integer.parseInt(v.getText().toString()), (double) SeekBarPreference.this.minimum, (double) SeekBarPreference.this.maximum);
                monitorBox.setText(String.valueOf(SeekBarPreference.this.currentValue));
                bar.setProgress(SeekBarPreference.this.currentValue - SeekBarPreference.this.minimum, true);
                SeekBarPreference.this.changer.onPreferenceChange(SeekBarPreference.this, Integer.toString(SeekBarPreference.this.currentValue));
                return true;
            }
        });
        bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int progress2 = Math.round(((float) progress) / ((float) SeekBarPreference.this.interval)) * SeekBarPreference.this.interval;
                SeekBarPreference.this.currentValue = SeekBarPreference.this.minimum + progress2;
                monitorBox.setText(String.valueOf(SeekBarPreference.this.currentValue));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                SeekBarPreference.this.changer.onPreferenceChange(SeekBarPreference.this, Integer.toString(SeekBarPreference.this.currentValue));
            }
        });
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        bind(view);
    }

    public void setInitValue(int progress) {
        this.currentValue = progress;
    }

    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        this.changer = onPreferenceChangeListener;
        super.setOnPreferenceChangeListener(onPreferenceChangeListener);
    }

    public int reset() {
        this.currentValue = (int) Utils.clamp((double) this.def, (double) this.minimum, (double) this.maximum);
        notifyChanged();
        return this.currentValue;
    }

    public void setValue(int progress) {
        this.currentValue = (int) Utils.clamp((double) progress, (double) this.minimum, (double) this.maximum);
        notifyChanged();
    }
}
