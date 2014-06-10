package com.salesforce.glassdemo.api;

import android.app.Activity;
import android.graphics.Bitmap;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import java.util.HashMap;
import java.util.Map;

public class SalesforceImageRequest extends ImageRequest {

    private Activity mContext;

    public SalesforceImageRequest(Activity context, String url, Response.Listener<Bitmap> listener, int maxWidth, int maxHeight, Bitmap.Config decodeConfig, Response.ErrorListener errorListener) {
        super(url, listener, maxWidth, maxHeight, decodeConfig, errorListener);
        mContext = context;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<String, String>();
        String auth = "Bearer " + SalesforceAPIManager.getAccessToken(mContext);
        headers.put("Authorization", auth);
        headers.put("Content-Type", "application/json");
        return headers;
    }

}
