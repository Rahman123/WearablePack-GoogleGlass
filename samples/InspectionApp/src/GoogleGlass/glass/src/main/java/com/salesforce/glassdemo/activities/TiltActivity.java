package com.salesforce.glassdemo.activities;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.salesforce.glassdemo.R;

// TODO: Finish converting class over to Inspection
public class TiltActivity extends Activity {
    private static final float HEAD_WAKE_ANGLE = 15.0f;
    /**
     * Handler used to post requests to start new activities so that the menu closing animation
     * works properly.
     */
    private final Handler mHandler = new Handler();
    /**
     * Listener that displays the options menu when the touchpad is tapped.
     */
    private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            if (gesture == Gesture.TAP) {
                mAudioManager.playSoundEffect(Sounds.TAP);
                return true;
            } else {
                return false;
            }
        }
    };
    /**
     * Audio manager used to play system sound effects.
     */
    private AudioManager mAudioManager;
    /**
     * Gesture detector used to present the options menu.
     */
    private GestureDetector mGestureDetector;
    private TextView tiltLabel;

    private SensorManager mSensorManager;
    private Sensor mOrientation;

    private transient float old_azimuth_angle = 0.0f;
    private transient float old_pitch_angle = 0.0f;
    private transient float old_roll_angle = 0.0f;
    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (old_azimuth_angle == 0.0f && old_pitch_angle == 0.0f && old_roll_angle == 0.0f) {
                old_azimuth_angle = event.values[0];
                old_pitch_angle = event.values[1];
                old_roll_angle = event.values[2];
            }

            float azimuth_angle = event.values[0];
            float pitch_angle = event.values[1];
            float roll_angle = event.values[2];

            tiltLabel.setText("Tilt: " + pitch_angle);
            System.out.printf("pitch_angle: %f\t\tdiff: %f", pitch_angle, old_pitch_angle - pitch_angle);

            if (old_pitch_angle - pitch_angle > HEAD_WAKE_ANGLE) {
                System.out.println("Head tilt UP detected...");
            } else if (old_pitch_angle - pitch_angle < -1 * HEAD_WAKE_ANGLE) {
                System.out.println("Head tilt DOWN detected...");
            } else {
                System.out.println(azimuth_angle + ", " + pitch_angle + ", " + roll_angle);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            System.out.println("Accuracy changed: sensor: " + sensor.toString() + ", new accuracy: " + accuracy);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tilt);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        tiltLabel = (TextView) findViewById(R.id.tilt_text);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this);
        mGestureDetector.setBaseListener(mBaseListener);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        mSensorManager.registerListener(sensorEventListener, mOrientation, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }
}
