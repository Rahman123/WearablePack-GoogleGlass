package com.salesforce.glassdemo.activities;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.glass.input.VoiceInputHelper;
import com.google.glass.input.VoiceListener;
import com.google.glass.logging.FormattingLogger;
import com.google.glass.logging.FormattingLoggers;
import com.google.glass.voice.VoiceCommand;
import com.google.glass.voice.VoiceConfig;
import com.salesforce.glassdemo.R;

import java.util.Arrays;

// TODO: Finish converting class over to Inspection
public class HotwordActivity extends Activity {
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
                setupVoice();
                setReady();
                return true;
            } else {
                return false;
            }
        }
    };
    TextView label;
    TextView footer;
    String[] items = {"red", "green", "blue", "orange"};
    /**
     * Audio manager used to play system sound effects.
     */
    private AudioManager mAudioManager;
    /**
     * Gesture detector used to present the options menu.
     */
    private GestureDetector mGestureDetector;
    private VoiceInputHelper mVoiceInputHelper;
    private VoiceConfig mVoiceConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_hotword);
        label = (TextView) findViewById(R.id.hotword_label);
        footer = (TextView) findViewById(R.id.footer);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this);
        mGestureDetector.setBaseListener(mBaseListener);

        footer.setText(Arrays.toString(items));
        setupVoice();
    }

    void setupVoice() {
        if (mVoiceInputHelper != null) mVoiceInputHelper.removeVoiceServiceListener();
        mVoiceConfig = new VoiceConfig("MyVoiceConfig", items);
        mVoiceInputHelper = new VoiceInputHelper(this, new MyVoiceListener(mVoiceConfig), VoiceInputHelper.newUserActivityObserver(this));
        mVoiceInputHelper.addVoiceServiceListener();
        setReady();
    }

    void setReady() {
        label.setText("Hotword Listening");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVoiceInputHelper.addVoiceServiceListener();
        setReady();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVoiceInputHelper.removeVoiceServiceListener();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }

    void setResponse(String s) {
        label.setText("Heard " + s);
    }

    public class MyVoiceListener implements VoiceListener {
        protected final VoiceConfig voiceConfig;

        public MyVoiceListener(VoiceConfig voiceConfig) {
            this.voiceConfig = voiceConfig;
        }

        @Override
        public FormattingLogger getLogger() {
            return FormattingLoggers.getContextLogger();
        }

        @Override
        public boolean isRunning() {
            return true;
        }

        @Override
        public boolean onResampledAudioData(byte[] arg0, int arg1, int arg2) {
            return false;
        }

        @Override
        public boolean onVoiceAmplitudeChanged(double arg0) {
            return false;
        }

        @Override
        public VoiceConfig onVoiceCommand(VoiceCommand vc) {
            String recognizedStr = vc.getLiteral();
            Log.i("VoiceActivity", "Recognized text: " + recognizedStr);
            setResponse(recognizedStr);
            return voiceConfig;
        }

        @Override
        public void onVoiceConfigChanged(VoiceConfig arg0, boolean arg1) {

        }

        @Override
        public void onVoiceServiceConnected() {
            mVoiceInputHelper.setVoiceConfig(mVoiceConfig); /* , false */
        }

        @Override
        public void onVoiceServiceDisconnected() {

        }
    }
}
