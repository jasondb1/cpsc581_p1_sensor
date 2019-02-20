package com.example.opensensor;

import android.annotation.SuppressLint;
import android.graphics.drawable.AnimationDrawable;
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
import android.widget.ImageView;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements SensorEventListener {

    /**
     * class attributes
     */
    public static boolean DEBUG = true;
    public static double PI = 3.1415926;
    private TextView mDebugText;
    private TextView mDebugLight;
    private SensorManager sManager;
    private Sensor mGyro;
    private Sensor mAccelerometer;
    private Sensor mMagneticField;
    private Sensor mLight;
    private EyeStatus eyeStatus = EyeStatus.OPEN;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private FrameLayout mPupils;
    private ImageView mEyelidLeft;
    private ImageView mEyelidRight;

    private enum EyeStatus {OPEN, CLOSED, CLOSING, OPENING};
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


    //TODO: The following will likely be disabled
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    /**
     * Activity is created
     *
     * This is where most of the setup of listeners etc should be initialized
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);


        //bind debugging fields
        if (DEBUG) {
            mDebugText = (TextView) findViewById(R.id.debug);
            mDebugText.setText("Running...");
            mDebugLight = (TextView) findViewById(R.id.debug_light);
            mDebugLight.setText("Running...");
        }

        //bind Pupils and eyelids
        mPupils = (FrameLayout) findViewById(R.id.pupils);
        mEyelidLeft = (ImageView) findViewById(R.id.EyelidLeft);
        mEyelidRight = (ImageView) findViewById(R.id.EyelidRight);

        //setup sensor manager
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGyro = sManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccelerometer = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLight = sManager.getDefaultSensor(Sensor.TYPE_LIGHT);


        //these were part of the template
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
    protected void onResume() {
        super.onResume();

        //register listeners
        //sManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);

        if (mAccelerometer != null) {
            sManager.registerListener(this, mAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (mMagneticField != null) {
            sManager.registerListener(this, mMagneticField,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mLight != null) {
            sManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);

        }

    }

    /**
     * Un-Registers mGyro and mLight listener when app is running
     */
    @Override
    protected void onPause() {
        super.onPause();

        //unregister listener when app is paused
        sManager.unregisterListener(this);
    }

    /**
     * Process changes when sensor data is changed
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor == mAccelerometer) {

            // May need this on a real phone
            //if sensor is unreliable, return void
            //if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
            //{
            //    mDebugText.setText("BAD DATA");
            //    return;
            //}

            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);

            updateOrientationAngles();
            updatePupils();

            if (DEBUG){
                mDebugText.setText("Azimuth:" + Float.toString(orientationAngles[0]) + "\n" +
                        "Pitch:" + Float.toString(orientationAngles[1]) + "\n" +
                        "Roll:" + Float.toString(orientationAngles[2]));

            }
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnetometerReading,
                        0, magnetometerReading.length);

            updateOrientationAngles();
            updatePupils();

            if (DEBUG){
                mDebugText.setText("Azimuth:" + Float.toString(orientationAngles[0]) + "\n" +
                        "Pitch:" + Float.toString(orientationAngles[1]) + "\n" +
                        "Roll:" + Float.toString(orientationAngles[2]));

            }
        } else if (event.sensor == mGyro) {

            //if sensor is unreliable, return void
            if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                return;
            }

        } else if (event.sensor == mLight) {
            //if sensor is unreliable, return void
            if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                return;
            }

            if (DEBUG) {
                mDebugLight.setText("Light:" + Float.toString(event.values[0]));
            }

            //TODO: Animate eye closing
            if (event.values[0] < 100 && (eyeStatus != EyeStatus.CLOSING) ){
                closeEyes();
            } else if (event.values[0] > 1000 && (eyeStatus != EyeStatus.OPENING)) {
                openEyes();
            }


        }
    }

    /**
     * Update the orientation angles, so pitch and roll can be read directly
     * instead of calculated
     */
    public void updateOrientationAngles() {
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        SensorManager.getOrientation(rotationMatrix, orientationAngles);
    }

    /**
     * (NOT USED) need to keep stub for interface
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Moves the pupils left/right up/down
     */
    private void updatePupils(){

        //set angle boundaries
        double LEFTBOUND = -PI / 6;
        double RIGHTBOUND = PI / 6;
        double UPPERBOUND = -PI / 2; //straight vertical
        double LOWERBOUND = PI / 6;

        //this is used to convert to screen dp
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float MOVE_H_FACTOR = 65.0f; //multiply the angle by this factor so pupil movement = roll * MOVE_FACTOR - This affects how far the eye moves horizontally
        float MOVE_V_FACTOR = 11.0f; //multiply the angle by this factor so pupil movement = roll * MOVE_FACTOR - This affects how far the eye moves vertically
        int offsetX = 5;    //Default Horizontal position of eyes without any movement
        int offsetY = 95;  //Default Vertical position of eyes without any movement
        float offsetAngleY = (float) (PI / 3); //in Radians - Used to adjust the angle at which the eyes are centered vertically

        float amount = 0.0f;
        int pixels = 0;

        //Move eyes horizontally
        if (orientationAngles[2] < LEFTBOUND) {
            amount = (float) (offsetX + (MOVE_H_FACTOR * LEFTBOUND));

        } else if (orientationAngles[2] > RIGHTBOUND) {
            amount = (float) (offsetX + (MOVE_H_FACTOR * RIGHTBOUND));
        } else {
            amount = (MOVE_H_FACTOR * orientationAngles[2]) + offsetX;
        }
        amount = metrics.density * amount;
        pixels = (int) (amount + 0.5f);
        mPupils.setTranslationX(pixels);


        //Move Eyes Vertically
        if (orientationAngles[1] < UPPERBOUND) {
            amount = (float) (offsetY + (MOVE_V_FACTOR * UPPERBOUND));

        } else if (orientationAngles[1] > LOWERBOUND) {
            amount = (float) (offsetY + (MOVE_V_FACTOR * LOWERBOUND));
        } else {
            amount = (MOVE_V_FACTOR * (orientationAngles[1] + offsetAngleY) ) + offsetY;
        }
        amount = metrics.density * amount;
        pixels = (int) (amount + 0.5f);
        mPupils.setTranslationY(pixels);
    }

    /**
     * Closes the eyes
     */
    private void closeEyes(){
        
        eyeStatus = EyeStatus.CLOSING;
        mEyelidLeft.setImageResource(R.drawable.eyesclose);
        mEyelidRight.setImageResource(R.drawable.eyesclose);
        AnimationDrawable eyeCloseLeft = (AnimationDrawable) mEyelidLeft.getDrawable();
        AnimationDrawable eyeCloseRight = (AnimationDrawable) mEyelidRight.getDrawable();
        eyeCloseLeft.start();
        eyeCloseRight.start();



        //mEyelidLeft.setImageResource(R.drawable.eyelid07);
        //mEyelidRight.setImageResource(R.drawable.eyelid07);


    }

    /**
     * Closes the eyes
     */
    private void openEyes(){

        eyeStatus = EyeStatus.OPENING;
        mEyelidLeft.setImageResource(R.drawable.eyesopen);
        mEyelidRight.setImageResource(R.drawable.eyesopen);
        AnimationDrawable eyeOpenLeft = (AnimationDrawable) mEyelidLeft.getDrawable();
        AnimationDrawable eyeOpenRight = (AnimationDrawable) mEyelidRight.getDrawable();
        eyeOpenLeft.start();
        eyeOpenRight.start();

        //mEyelidLeft.setImageResource(R.drawable.eyelid00);
        //mEyelidRight.setImageResource(R.drawable.eyelid00);

    }

}
