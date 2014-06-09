package com.salesforce.glassdemo.voice;

import android.content.Context;

import com.google.glass.input.VoiceInputHelper;
import com.google.glass.voice.VoiceCommand;
import com.google.glass.voice.VoiceConfig;

public class VoiceDetection extends StubVoiceListener {
    private final VoiceConfig mVoiceConfig;
    private String[] mPhrases;
    private VoiceInputHelper mVoiceInputHelper;
    private VoiceDetectionListener mListener;
    private boolean mRunning = true;

    public VoiceDetection(Context context, String hotword, VoiceDetectionListener listener, String... phrases) {
        mVoiceInputHelper = new VoiceInputHelper(context, this, VoiceInputHelper.newUserActivityObserver(context));

        mPhrases = assemblePhrases(hotword, phrases);

        mVoiceConfig = new VoiceConfig("VoiceDetection", mPhrases);
        mVoiceConfig.setShouldSaveAudio(false);
        mListener = listener;
    }

    private String[] assemblePhrases(String hotword, String[] phrases) {
        String[] res = new String[phrases.length + 1];
        res[0] = hotword;
        for (int i = 0; i < phrases.length; ++i)
            res[i + 1] = phrases[i];

        return res;
    }

    public void changePhrases(String... phrases) {
        mPhrases = assemblePhrases(mPhrases[0], phrases);
        mVoiceConfig.setCustomPhrases(mPhrases);
        //mVoiceInputHelper.setVoiceConfig(mVoiceConfig, false);
        mVoiceInputHelper.setVoiceConfig(mVoiceConfig);
    }

    @Override
    public void onVoiceServiceConnected() {
        super.onVoiceServiceConnected();
        mVoiceInputHelper.setVoiceConfig(mVoiceConfig);
    }

    @Override
    public VoiceConfig onVoiceCommand(VoiceCommand vc) {
        String literal = vc.getLiteral();

        if (literal.equals(mPhrases[0])) {
            mListener.onHotwordDetected();
        }

        for (int i = 1; i < mPhrases.length; ++i) {
            String item = mPhrases[i];
            if (item.equals(literal)) {
                mListener.onPhraseDetected(i - 1, literal);
                return null;
            }
        }

        return null;
    }

    public void start() {
        mRunning = true;
        mVoiceInputHelper.addVoiceServiceListener();
    }

    public void stop() {
        mRunning = false;
        mVoiceInputHelper.removeVoiceServiceListener();
    }

    @Override
    public boolean isRunning() {
        return mRunning;
    }

    public interface VoiceDetectionListener {
        public void onHotwordDetected();

        public void onPhraseDetected(int index, String phrase);
    }
}
