package com.salesforce.glassdemo.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.glass.app.Card;
import com.google.android.glass.media.CameraManager;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.salesforce.glassdemo.Constants;
import com.salesforce.glassdemo.Data;
import com.salesforce.glassdemo.R;
import com.salesforce.glassdemo.api.SalesforceAPIManager;
import com.salesforce.glassdemo.cards.StepCard;
import com.salesforce.glassdemo.models.Inspection;
import com.salesforce.glassdemo.models.InspectionResponse;
import com.salesforce.glassdemo.models.InspectionStep;
import com.salesforce.glassdemo.models.InspectionStepResponse;
import com.salesforce.glassdemo.util.CameraUtils;
import com.salesforce.glassdemo.util.PhotoCache;
import com.salesforce.glassdemo.voice.VoiceDetection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InspectionActivity extends Activity {
    private static final int REQUESTCODE_NEW_CASE_TRANSCRIPTION = 1000;
    private static final int REQUESTCODE_PICTURE = 1001;
    private static final int REQUESTCODE_DICTATE = 1002;
    /**
     * Listener that displays the options menu when the touchpad is tapped.
     */
    private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            Log.i(Constants.TAG, "Gesture detected: " + gesture.toString());

            if (gesture == Gesture.TAP) {
                mAudioManager.playSoundEffect(Sounds.TAP);
                if (mCardScrollView.getSelectedItemPosition() == mCards.size() - 1) {
                    goRelaunchApplication();
                }
                //openOptionsMenu();
                return true;
            } else if (gesture == Gesture.TWO_SWIPE_RIGHT) {
                goNextStep();
                return true;
            } else if (gesture == Gesture.SWIPE_DOWN) {
                // this is the back gesture, which we probably don't want to swallow yet // TODO
                return false;
            } else if (gesture == Gesture.LONG_PRESS) {
                goFinishCard();
                return true;
            } else if (gesture == Gesture.TWO_TAP) {
                voiceDetectionListener.onPhraseDetected(0, "affirmative");
                return true;
            } else if (gesture == Gesture.TWO_LONG_PRESS) {
                voiceDetectionListener.onPhraseDetected(0, "negative");
                return true;
            } else if (gesture == Gesture.THREE_TAP) {
                voiceDetectionListener.onPhraseDetected(0, "success");
                return true;
            } else if (gesture == Gesture.THREE_LONG_PRESS) {
                voiceDetectionListener.onPhraseDetected(0, "failure");
                return true;
            } else if (gesture == Gesture.TWO_SWIPE_UP) {
                voiceDetectionListener.onPhraseDetected(0, "picture");
                return true;
            } else {
                return false;
            }
        }
    };

    private VoiceDetection.VoiceDetectionListener voiceDetectionListener = new VoiceDetection.VoiceDetectionListener() {
        @Override
        public void onHotwordDetected() {
            Log.i(Constants.TAG, "Hotword detected!");
        }

        @Override
        public void onPhraseDetected(int index, String phrase) {
            phrase = phrase.toLowerCase();

            Log.i(Constants.TAG, "Got phrase: " + phrase);
            mVoiceDetection.stop();
            mVoiceDetection.start();

            // For the first card, only one voice command is accessible.
            if (mCardScrollView.getSelectedItemPosition() == 0) {
                if (phrase.equals("next")) {
                    goNextStep();
                }
                return;
            }

            if (!photoCache.isReady()) {
                Log.i(Constants.TAG, "Cannot continue until photos are done downloading");
                return;
            }

            InspectionStep step = getInspectionStep();

            // If we aren't on a valid InspectionStep, the voice command is invalid.
            if (step == null) {
                return;
            }

            // These commands affect every step type
            if (phrase.equals("picture")) {
                isr.hasPhoto = true;
                startPictureActivity();
                return;
            }

            if (phrase.equals("documentation")) {
                Log.i(Constants.TAG, "Documentation");
                showDocumentation(step.id);
                return;
            }

            if (phrase.equals("skip")) {
                goNextStep();
                return;
            }

            // Yes/No question
            if (step.type.equals(Constants.InspectionTypes.TYPE_AFFIRMATIVE_NEGATIVE)) {
                if (phrase.equals("affirmative")) {
                    isr.answer = phrase;
                    isr.inspectionResponseId = inspectionResponse.id;
                    postStepResponse(isr);
                    startNewCaseDictation();
                    return;
                } else if (phrase.equals("negative")) {
                    isr.answer = phrase;
                    isr.inspectionResponseId = inspectionResponse.id;
                    postStepResponse(isr);
                    goNextStep();
                    return;
                }
            }

            if (step.type.equals(Constants.InspectionTypes.TYPE_SUCCESS_FAILURE)) {
                if (phrase.equals("success")) {
                    isr.answer = phrase;
                    isr.inspectionResponseId = inspectionResponse.id;
                    postStepResponse(isr);
                    goNextStep();
                    return;
                } else if (phrase.equals("failure")) {
                    isr.answer = phrase;
                    isr.inspectionResponseId = inspectionResponse.id;
                    postStepResponse(isr);
                    startNewCaseDictation();
                    return;
                }
            }

            if (step.type.equals(Constants.InspectionTypes.TYPE_TEXT) || step.type.equals(Constants.InspectionTypes.TYPE_NUMBER)) {
                if (phrase.equals("dictate")) {
                    startDictateActivity();
                    return;
                }
            }
        }
    };


    /**
     * Audio manager used to play system sound effects.
     */
    private AudioManager mAudioManager;

    /**
     * Handler used to post requests to start new activities so that the menu closing animation
     * works properly.
     */
    private final Handler mHandler = new Handler();

    /**
     * Gesture detector used to present the options menu.
     */
    private GestureDetector mGestureDetector;

    private List<Card> mCards;
    private CardScrollView mCardScrollView;
    private PhotoCache photoCache = new PhotoCache();

    Inspection inspection;
    private VoiceDetection mVoiceDetection;
    private InspectionResponse inspectionResponse;
    private InspectionStepResponse isr;
    private InspectionStepCardScrollAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);
        mCardScrollView = (CardScrollView) findViewById(R.id.card_scroll_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle b = getIntent().getExtras();
        final String siteId = b.getString("siteId");
        final String inspectionId = b.getString("inspectionId");
        inspection = Data.getInstance().getInspectionFromSite(siteId, inspectionId);

        Log.i(Constants.TAG, "Started InspectionActivity " + siteId + " " + inspectionId);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this);
        mGestureDetector.setBaseListener(mBaseListener);

        mCards = new ArrayList<Card>();
        mCards.add(getTitleCard());
        adapter = new InspectionStepCardScrollAdapter();
        mCardScrollView.setAdapter(adapter);
        mCardScrollView.activate();
        mCardScrollView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final InspectionStep inspectionStep = getInspectionStep();
                if (inspectionStep != null) {
                    Log.i(Constants.TAG, "got step " + inspectionStep.text);
                    isr = new InspectionStepResponse(inspectionStep, inspectionResponse, "");
                } else {
                    Log.i(Constants.TAG, "got null");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        fetchData();

        /**
         * The CardScrollView regains focus when the cards are loaded.
         * As we can't easily see when that happens, we unfocus after 100ms,
         * under the assumption that the cards are done loading in that time.
         */
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                unfocusCardScrollView();
            }
        }, 100);

        final String[] phrases = {
                "affirmative", "negative", // yes/no question
                "success", "failure", // pass/fail question
                "dictate", // text and number entry
                "documentation", // documentation, for any question
                "picture", // for taking a picture in the field
                "skip", // to skip a question
                "next", // to begin
        };
        mVoiceDetection = new VoiceDetection(this, "ok glass", voiceDetectionListener, phrases);

        inspectionResponse = new InspectionResponse(inspection);
        SalesforceAPIManager.postInspectionResponse(this, inspectionResponse,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            inspectionResponse.id = jsonObject.getString("id");
                            Log.i(Constants.TAG, "Received InspectionResponse ID " + inspectionResponse.id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVoiceDetection.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVoiceDetection.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUESTCODE_NEW_CASE_TRANSCRIPTION && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);

            SalesforceAPIManager.postNewCase(
                    this,
                    inspectionResponse,
                    getInspectionStep().text,
                    spokenText,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            Log.i(Constants.TAG, "Posted new case successfully. " + jsonObject.toString());
                        }
                    }
            );

            goNextStep();
        } else if (requestCode == REQUESTCODE_NEW_CASE_TRANSCRIPTION && resultCode == RESULT_CANCELED) {
            Log.w(Constants.TAG, "User canceled voice transcription for new case. Returning to previous location");
        } else if (requestCode == REQUESTCODE_PICTURE && resultCode == RESULT_OK) {
            //base64Image = data.getExtras().getString(CameraActivity.EXTRA_BASE64_IMAGE);
            Log.i(Constants.TAG, "Received picture successfully.");
            String picturePath = data.getStringExtra(CameraManager.EXTRA_PICTURE_FILE_PATH);
            processPictureWhenReady(picturePath, isr);
        } else if (requestCode == REQUESTCODE_PICTURE && resultCode == RESULT_CANCELED) {
            Log.i(Constants.TAG, "Picture taking cancelled.");
        } else if (requestCode == REQUESTCODE_DICTATE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            isr.answer = spokenText;
            postStepResponse(isr);
            goNextStep();
        } else if (requestCode == REQUESTCODE_DICTATE && resultCode == RESULT_CANCELED) {
            Log.w(Constants.TAG, "User canceled voice transcription for step. Returning to previous location");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void unfocusCardScrollView() {
        mCardScrollView.clearFocus();
        mCardScrollView.setFocusableInTouchMode(false);
        mCardScrollView.setFocusable(false);
    }

    private InspectionStep getInspectionStep() {
        final int pos = mCardScrollView.getSelectedItemPosition();
        int num = pos - 1;
        final int size = inspection.steps.size();

        if (num < 0 || num >= size) {
            return null;
        } else {
            return inspection.steps.get(num);
        }
    }

    private void goNextStep() {
        mCardScrollView.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
    }

    private void goFinishCard() {
        final int count = mCards.size();
        mCardScrollView.setSelection(count - 1);
    }

    private void goRelaunchApplication() {
        Log.i(Constants.TAG, "Restarting application...");
        startActivity(new Intent(InspectionActivity.this, SplashActivity.class));
        finish();
    }

    private void fetchData() {
        if (inspection.steps == null || inspection.steps.isEmpty()) {
            Log.e(Constants.TAG, "Inspection " + inspection.id + " has no steps. Quitting.");
            finish();
            return;
        }

        boolean needsFetch = false;
        for (final InspectionStep step : inspection.steps) {
            if (step.photoId != null && !step.photoId.isEmpty()) {
                // Put photo ID into PhotoCache
                photoCache.add(step.photoId);
                needsFetch = true;

                // Download the photo and add to the cache
                Response.Listener<Bitmap> listener = new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        Log.i(Constants.TAG, "Got bitmap back for step " + step.id);
                        photoCache.putBitmap(step.photoId, bitmap);

                        if (photoCache.isReady()) {
                            Log.i(Constants.TAG, "Photo cache is ready. Creating cards.");
                            createCards();
                            goNextStep();
                            adapter.notifyDataSetChanged();
                        }
                    }
                };
                Response.ErrorListener errorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e(Constants.TAG, volleyError.toString());
                        Log.i(Constants.TAG, "Removing photo from cache: " + step.photoId);
                        photoCache.remove(step.photoId);
                    }
                };
                SalesforceAPIManager.getAttachment(InspectionActivity.this, step, listener, errorListener);
            }
        }

        if (!needsFetch) {
            createCards();
            goNextStep();
            adapter.notifyDataSetChanged();
        }
    }

    private void createCards() {
        if (inspection.steps == null || inspection.steps.isEmpty()) {
            Log.e(Constants.TAG, "Inspection " + inspection.id + " has no steps. Quitting.");
            finish();
            return;
        }

        for (final InspectionStep step : inspection.steps) {
            final StepCard card = new StepCard(this, step);
            mCards.add(card);

            if (step.photoId != null && !step.photoId.isEmpty()) {
                Bitmap bitmap = photoCache.retrieveBitmap(step.photoId);
                if (bitmap != null) {
                    card.setImageLayout(Card.ImageLayout.FULL);
                    card.addImage(bitmap);
                }
            }
        }

        mCards.add(getDoneCard());
    }

    private Card getTitleCard() {
        Card card = new Card(this);
        card.setText(inspection.title);
        card.setFootnote("Loading data...");
        return card;
    }

    private Card getDoneCard() {
        Card doneCard = new Card(this);
        doneCard.setText("Inspection Completed");
        doneCard.setFootnote("Tap to select another inspection");
        return doneCard;
    }

    private void postStepResponse(final InspectionStepResponse sr) {
        Log.i(Constants.TAG, "Post new InspectionStepResponse " + sr);
        final Runnable delayRunnable = new Runnable() {
            @Override
            public void run() {
                postStepResponse(sr);
            }
        };
        if (inspectionResponse.inspectionId == null || inspectionResponse.inspectionId.isEmpty()) {
            Log.i(Constants.TAG, "InspectionResponse ID not set yet, postponing call");
            mHandler.postDelayed(delayRunnable, 1000);
        } else if (sr.hasPhoto && sr.photo == null) {
            Log.i(Constants.TAG, "Photo not yet received for step, delaying. " + inspectionResponse);
            mHandler.postDelayed(delayRunnable, 1000);
        } else {
            Log.i(Constants.TAG, "Submitting step response " + sr);
            SalesforceAPIManager.postInspectionStepResponse(
                    this,
                    sr,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            Log.i(Constants.TAG, "Got response: " + jsonObject);

                            String id;
                            try {
                                id = jsonObject.getString("id");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                id = "";
                            }
                            sr.id = id;

                            if (sr.hasPhoto) {
                                final String isrId = sr.id;
                                String filename = inspection.id + "-" + isrId + ".jpg";
                                Log.i(Constants.TAG, "Posting photo with ISR ID: " + isrId + " to API as filename " + filename + " and length: " + sr.photo.length());
                                sr.hasPhoto = false;
                                SalesforceAPIManager.postAttachment(
                                        InspectionActivity.this,
                                        isrId,
                                        filename,
                                        sr.photo,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject jsonObject) {
                                                Log.i(Constants.TAG, "Image posting successful: " + jsonObject.toString());
                                            }
                                        }
                                );
                            }
                        }
                    }
            );
        }
    }

    private void processPictureWhenReady(final String picturePath, final InspectionStepResponse inspectionStepResponse) {
        final File pictureFile = new File(picturePath);

        if (pictureFile.exists()) {
            // The picture is ready; process it.
            Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
            bitmap = CameraUtils.resizeImageToHalf(bitmap);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);
            String base64Image = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
            inspectionStepResponse.hasPhoto = true;
            inspectionStepResponse.photo = base64Image;
        } else {
// The file does not exist yet. Before starting the file observer, you
// can update your UI to let the user know that the application is
// waiting for the picture (for example, by displaying the thumbnail
// image and a progress indicator).

            final File parentDirectory = pictureFile.getParentFile();
            FileObserver observer = new FileObserver(parentDirectory.getPath(), FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO) {
// Protect against additional pending events after CLOSE_WRITE
// or MOVED_TO is handled.

                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        File affectedFile = new File(parentDirectory, path);
                        isFileWritten = affectedFile.equals(pictureFile);

                        if (isFileWritten) {
                            stopWatching();

                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath, inspectionStepResponse);
                                }
                            });
                        }
                    }
                }
            };
            observer.startWatching();
        }
    }

    private void startPictureActivity() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUESTCODE_PICTURE);
    }

    private void showDocumentation(final String inspectionStepId) {
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    JSONObject record = jsonObject.getJSONArray("records").getJSONObject(0);
                    Boolean hasDocumentation = !record.isNull("Inspections_Docs__r");
                    if (hasDocumentation) {
                        String link = record.getJSONObject("Inspections_Docs__r").getJSONArray("records").getJSONObject(0).getString("Video_Link__c");
                        Log.i(Constants.TAG, "Obtained documentation link: " + link);
                        Intent i = new Intent();
                        i.setAction("com.google.glass.action.VIDEOPLAYER");
                        i.putExtra("video_url", link);
                        startActivity(i);
                    } else {
                        startActivity(new Intent(InspectionActivity.this, NoDocumentationActivity.class));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        SalesforceAPIManager.getDocumentation(this, inspectionStepId, listener);
    }

    private void startDictateActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dictate the data");
        startActivityForResult(intent, REQUESTCODE_DICTATE);
    }

    private void startNewCaseDictation() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Explain the details of the problem");
        startActivityForResult(intent, REQUESTCODE_NEW_CASE_TRANSCRIPTION);
    }

    private class InspectionStepCardScrollAdapter extends CardScrollAdapter {
        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) {
            return mCards.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mCards.get(position).getView(convertView, parent);
        }

        /**
         * Returns the view type of this card so the system can figure out
         * if it can be recycled.
         */
        @Override
        public int getItemViewType(int position) {
            return mCards.get(position).getItemViewType();
        }

        /**
         * Returns the amount of view types.
         */
        @Override
        public int getViewTypeCount() {
            return Card.getViewTypeCount();
        }

        @Override
        public int getPosition(Object item) {
            return mCards.indexOf(item);
        }
    }
}
