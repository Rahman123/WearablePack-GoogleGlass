package com.salesforce.glassdemo.api;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SalesforceJSONObjectRequest extends JsonObjectRequest {

    private Context mContext;

    public SalesforceJSONObjectRequest(Context context, int method, String url, JSONObject jsonObject, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonObject, listener, errorListener);
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
