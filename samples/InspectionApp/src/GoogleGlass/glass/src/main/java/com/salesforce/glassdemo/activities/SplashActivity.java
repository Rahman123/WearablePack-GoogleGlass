package com.salesforce.glassdemo.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.salesforce.glassdemo.Data;
import com.salesforce.glassdemo.R;
import com.salesforce.glassdemo.api.SalesforceAPIManager;

public class SplashActivity extends Activity {
    /**
     * Listener that displays the options menu when the touchpad is tapped.
     */
    private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            if (gesture == Gesture.TAP || gesture == Gesture.SWIPE_RIGHT) {
                Log.i("SalesforceGlassDemo", "Gesture received: tap");
                if (Data.getInstance().allSites != null && !Data.getInstance().allSites.isEmpty()) {
                    mAudioManager.playSoundEffect(Sounds.SUCCESS);
                    startActivity(new Intent(SplashActivity.this, SelectSiteActivity.class));
                    finish(); // do not return to the splash screen
                }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this);
        mGestureDetector.setBaseListener(mBaseListener);

        SalesforceAPIManager.getGPSCoordinates(getApplicationContext());
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }
}
