package com.salesforce.glassdemo.util;

import android.graphics.Bitmap;
import android.util.Log;

import com.salesforce.glassdemo.Constants;

import java.util.HashMap;

public class PhotoCache {
    private HashMap<String, Bitmap> photoCache;

    public PhotoCache() {
        photoCache = new HashMap<String, Bitmap>();
    }

    /**
     * Add a new photo ID to the cache. The cache is not considered ready unless all photos are
     * downloaded successfully
     * @param photoId
     */
    public void add(String photoId) {
        photoCache.put(photoId, null);
    }

    /**
     * Remove a Photo ID from the cache.
     * This is useful if a photo downloads an error and we wish to not cache this photo.
     * @param photoId
     */
    public void remove(String photoId) {
        photoCache.remove(photoId);
    }

    /**
     * Put a bitmap once it is done downloading
     * @param photoId
     * @param bitmap
     */
    public void putBitmap(String photoId, Bitmap bitmap) {
        photoCache.put(photoId, bitmap);
    }

    /**
     * Retrieve a bitmap from the cache once the cache is ready.
     * @param photoId
     * @return
     */
    public Bitmap retrieveBitmap(String photoId) {
        return photoCache.get(photoId);
    }

    /**
     * Ensure that every photo has been downloaded
     * @return true if all photos ready, false otherwise
     */
    public boolean isReady() {
        int notReady = 0;
        for (Bitmap b : photoCache.values()) {
            if (b == null) notReady++;
        }
        Log.i(Constants.TAG, "Not ready is " + notReady);
        return notReady == 0;
    }
}
