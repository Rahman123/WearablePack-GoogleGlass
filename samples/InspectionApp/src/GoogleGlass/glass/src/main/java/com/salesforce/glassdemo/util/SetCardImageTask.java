package com.salesforce.glassdemo.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.glass.app.Card;
import com.salesforce.glassdemo.Constants;

import java.io.InputStream;

public class SetCardImageTask extends AsyncTask<String, Void, Bitmap> {
    protected Card card;
    protected String imageUrl;

    public SetCardImageTask(Card card) {
        this.card = card;
    }

    protected Bitmap doInBackground(String... urls) {
        imageUrl = urls[0];

        Bitmap bitmap = null;
        try {
            InputStream in = new java.net.URL(imageUrl).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e(Constants.TAG, e.getMessage());
            e.printStackTrace();
        }
        return bitmap;
    }

    protected void onPostExecute(Bitmap result) {
        if (result != null) {
            card.addImage(result);
            card.setImageLayout(Card.ImageLayout.FULL);
        } else {
            Log.w(Constants.TAG, "Image was unable to be downloaded from URL: " + imageUrl);
        }
    }
}
