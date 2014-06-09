package com.salesforce.glassdemo.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.salesforce.glassdemo.Constants;
import com.salesforce.glassdemo.Data;
import com.salesforce.glassdemo.R;
import com.salesforce.glassdemo.models.InspectionSite;

import java.util.ArrayList;

// TODO: Finish converting class over to Inspection
public class MainActivity extends Activity {
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
                Log.i("SalesforceGlassDemo", "Gesture received: tap");
                mAudioManager.playSoundEffect(Sounds.TAP);
                openOptionsMenu();
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

        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this);
        mGestureDetector.setBaseListener(mBaseListener);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * The act of starting an activity here is wrapped inside a posted {@code Runnable} to avoid
     * animation problems between the closing menu and the new activity. The post ensures that the
     * menu gets the chance to slide down off the screen before the activity is started.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The startXXX() methods start a new activity, and if we call them directly here then
        // the new activity will start without giving the menu a chance to slide back down first.
        // By posting the calls to a handler instead, they will be processed on an upcoming pass
        // through the message queue, after the animation has completed, which results in a
        // smoother transition between activities.
        switch (item.getItemId()) {
            /*case R.id.option1:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //if (canAdvance()) startActivity(new Intent(this, SomeActivity.class));
                        finish(); // to prevent this screen from reappearing.
                    }
                });
                return true;*/

            case R.id.inspect:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(Constants.TAG, "Starting inspection...");
                        if (canAdvance())
                            startActivity(new Intent(getBaseContext(), SelectSiteActivity.class));
                    }
                });
                return true;

            case R.id.hotword:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(getBaseContext(), HotwordActivity.class));
                    }
                });
                return true;

            case R.id.tilt_test:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(getBaseContext(), TiltActivity.class));
                    }
                });
                return true;

            case R.id.video:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(getBaseContext(), VideoActivity.class));
                    }
                });
                return true;

            default:
                return false;
        }
    }

    public boolean canAdvance() {
        final ArrayList<InspectionSite> allSites = Data.getInstance().allSites;
        if (allSites != null && !allSites.isEmpty()) {
            mAudioManager.playSoundEffect(Sounds.SELECTED);
            Log.i(Constants.TAG, "Can advance! Onward!");
            return true;
        } else {
            Log.w(Constants.TAG, "Can't advance yet, warning");
            mAudioManager.playSoundEffect(Sounds.DISALLOWED);
            return false;
        }
    }

}
