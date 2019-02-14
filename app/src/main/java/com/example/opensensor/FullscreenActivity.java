package com.example.opensensor;

import android.annotation.SuppressLint;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements SensorEventListener {

    private TextView mDebugText;
    private TextView mDebugLight;
    private SensorManager sManager;
    private Sensor mGyro;
    private Sensor mAccelerometer;
    private Sensor mLight;


    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */


    //TODO: This will likely be disabled

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        //setup sensor manager

        mDebugText = (TextView) findViewById(R.id.debug);
        mDebugText.setText("Running...");
        mDebugLight = (TextView) findViewById(R.id.debug_light);
        mDebugLight.setText("Running...");

        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGyro = sManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccelerometer = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLight = sManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Registers mGyro and mLight listener when app is running
     */
    @Override
    protected void onResume(){
        super.onResume();

        //register listeners
        //sManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
        sManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //TODO: explore method Sensor.requestTriggerSensor
        sManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);

        }

    /**
     *  Un-Registers mGyro and mLight listener when app is running
     */
    @Override
    protected void onPause(){
        super.onPause();

        //unregister listener when app is paused
        sManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {


        if (event.sensor == mAccelerometer) {

            //if sensor is unreliable, return void
            //if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
            //{
            //    mDebugText.setText("BAD DATA");
            //    return;
            //}

            //else it will output the Roll, Pitch and Yawn values
            mDebugText.setText("X:"+ Float.toString(event.values[2]) +"\n"+
                    "Y:"+ Float.toString(event.values[1]) +"\n"+
                    "Z:"+ Float.toString(event.values[0]));
        }
        else if (event.sensor == mGyro) {

            //if sensor is unreliable, return void
            if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
            {
                return;
            }

            //else it will output the Roll, Pitch and Yawn values
            mDebugText.setText("X:"+ Float.toString(event.values[2]) +"\n"+
                    "Y:"+ Float.toString(event.values[1]) +"\n"+
                    "Z:"+ Float.toString(event.values[0]));
        }
        else if (event.sensor == mLight) {
            //if sensor is unreliable, return void
            if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
            {
                return;
            }
            //else it will output the Roll, Pitch and Yawn values
            //debugText.setText("X:"+ Float.toString(event.values[0]);
            mDebugLight.setText("Light:" + Float.toString(event.values[0]));
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
