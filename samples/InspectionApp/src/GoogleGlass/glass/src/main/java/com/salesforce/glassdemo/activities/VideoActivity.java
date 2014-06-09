package com.salesforce.glassdemo.activities;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.VideoView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.salesforce.glassdemo.R;

// TODO: Finish converting class over to Inspection
public class VideoActivity extends Activity {
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
                mVideoView.pause();
                return true;
            } else {
                return false;
            }
        }
    };
    VideoView mVideoView;
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

        setContentView(R.layout.activity_video);

        mVideoView = (VideoView) findViewById(R.id.video);
        String fileName = "android.resource://" + getPackageName() + "/" + R.raw.out;
        mVideoView.setVideoURI(Uri.parse(fileName));
        mVideoView.requestFocus();
        mVideoView.start();
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                finish();
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this);
        mGestureDetector.setBaseListener(mBaseListener);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }
}
