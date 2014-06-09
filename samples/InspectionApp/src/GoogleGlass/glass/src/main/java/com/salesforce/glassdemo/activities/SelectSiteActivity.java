package com.salesforce.glassdemo.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.salesforce.glassdemo.Constants;
import com.salesforce.glassdemo.Data;
import com.salesforce.glassdemo.R;
import com.salesforce.glassdemo.cards.SiteCard;
import com.salesforce.glassdemo.models.InspectionSite;

import java.util.ArrayList;
import java.util.List;

public class SelectSiteActivity extends Activity {
    Handler mHandler = new Handler();
    /**
     * Audio manager used to play system sound effects.
     */
    private AudioManager mAudioManager;
    /**
     * Gesture detector used to present the options menu.
     */
    private List<Card> mCards;
    private CardScrollView mCardScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectsite);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mCards = new ArrayList<Card>();
        for (InspectionSite site : Data.getInstance().allSites) {
            mCards.add(new SiteCard(this, site));
        }

        mCardScrollView = (CardScrollView) findViewById(R.id.card_scroll_view);
        mCardScrollView.setAdapter(new InspectionSiteCardScrollAdapter());
        mCardScrollView.activate();
        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SiteCard card = (SiteCard) mCardScrollView.getAdapter().getItem(position);
                        InspectionSite site = card.site;

                        if (site.inspections != null && !site.inspections.isEmpty()) {
                            mAudioManager.playSoundEffect(Sounds.TAP);
                            Log.i(Constants.TAG, "Starting inspection for site " + site.name + "...");
                            final Intent intent = new Intent(SelectSiteActivity.this, SelectInspectionActivity.class);
                            intent.putExtra("siteId", site.id);
                            startActivity(intent);
                            //finish();
                        } else {
                            mAudioManager.playSoundEffect(Sounds.DISALLOWED);
                        }
                    }
                });
            }
        });
    }

    private class InspectionSiteCardScrollAdapter extends CardScrollAdapter {
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