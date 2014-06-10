package com.salesforce.glassdemo.api;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.salesforce.glassdemo.Constants;
import com.salesforce.glassdemo.Data;
import com.salesforce.glassdemo.models.Inspection;
import com.salesforce.glassdemo.models.InspectionResponse;
import com.salesforce.glassdemo.models.InspectionSite;
import com.salesforce.glassdemo.models.InspectionStep;
import com.salesforce.glassdemo.models.InspectionStepResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesforceAPIManager {
    public static String SOQL_QUERY_PATH = "/services/data/v30.0/query/?q=";

    public static String getInstanceUrl(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.TAG_PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getString(Constants.TAG_INSTANCE_URL, Constants.SALESFORCE_INSTANCE_URL);
    }

    public static String getRefreshToken(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType("com.salesforce.glassdemo");
        if (accounts.length == 0) {
            Log.e(Constants.TAG, "Got no accounts!");
            return "";
        }

        Log.i(Constants.TAG, "Accounts: ");
        for (Account a: accounts) {
            Log.i(Constants.TAG, "Got account: " + a.name + " " + a.type);
        }

        Account account = accounts[0];
        return accountManager.getPassword(account);
    }

    public static String getAccessToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.TAG_PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getString(Constants.TAG_ACCESS_TOKEN, null);
    }

    public static void getNewAccessToken(final Activity context, final Response.Listener<String> listener) {
        String url = "https://login.salesforce.com/services/oauth2/token";
        Response.Listener<String> stringListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                JSONObject jsonResponse;
                try {
                    jsonResponse = new JSONObject(s);
                    String accessToken = jsonResponse.getString(Constants.ACCESS_TOKEN);
                    String instanceUrl = jsonResponse.getString(Constants.INSTANCE_URL);

                    SharedPreferences prefs = context.getSharedPreferences(Constants.TAG_PREFERENCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Constants.TAG_ACCESS_TOKEN, accessToken);
                    editor.putString(Constants.TAG_INSTANCE_URL, instanceUrl);
                    editor.commit();
                    listener.onResponse(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(Constants.TAG, "Unable to obtain a new Access Token.  Quitting.");
                context.finish();
            }
        };

        StringRequest myReq = new StringRequest(Request.Method.POST,
                url,
                stringListener,
                errorListener) {

            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "refresh_token");
                params.put("client_id", Constants.CLIENT_ID);
                params.put("client_secret", Constants.CLIENT_SECRET);
                params.put("refresh_token", getRefreshToken(context));
                return params;
            }
        };

        VolleySingleton.getInstance(context).getRequestQueue().add(myReq);
    }

    public static void getNewRefreshToken(final Activity context, final Response.Listener<String> listener) {
        /*
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType("com.salesforce.glassdemo");
        if (accounts.length == 0) {
            Log.e(Constants.TAG, "Got no accounts!");
            return;
        }

        Log.i(Constants.TAG, "Accounts: ");
        for (Account a: accounts) {
            Log.i(Constants.TAG, "Got account: " + a.name + " " + a.type);
        }

        Account account = accounts[0];
        final String AUTH_TOKEN_TYPE = "refresh_token";

        accountManager.getAuthToken(account, AUTH_TOKEN_TYPE, new Bundle(), context, new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    String refreshToken = future.getResult().getString("refreshtoken");
                    Log.i(Constants.TAG, "Got refreshToken " + (refreshToken != null ? refreshToken : "(null)"));

                    SharedPreferences prefs = context.getSharedPreferences(Constants.TAG_PREFERENCES, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(Constants.TAG_REFRESH_TOKEN, refreshToken);
                    editor.commit();

                    listener.onResponse(refreshToken);
                } catch (Exception e) {
                    Log.i(Constants.TAG, "Exception: unable to get Refresh Token", e);
                    // Handle exception.
                }
            }
        }, null);

        Log.i(Constants.TAG, "Password: " + accountManager.getPassword(account));
        */
    }

    public static void getGPSCoordinates(final Activity context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);

        final Handler h = new Handler();
        final Runnable fakeGpsLocationRunnable = new Runnable() {
            @Override
            public void run() {
                Log.i(Constants.TAG, "Requesting data with fake coordinates");
                getSitesForCoordinates(context, 37.784067, -122.402953);
            }
        };
        h.postDelayed(fakeGpsLocationRunnable, 5 * 1000);


        List<String> providers = locationManager.getProviders(criteria, true);
        for (String provider : providers) {
            locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    h.removeCallbacks(fakeGpsLocationRunnable);

                    final double latitude = location.getLatitude();
                    final double longitude = location.getLongitude();

                    Log.i(Constants.TAG, "Get location: " + latitude + " " + longitude);
                    if (Data.getInstance().allSites.isEmpty()) {
                        getSitesForCoordinates(context, latitude, longitude);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
    }

    private static void getSitesForCoordinates(final Activity context, final double latitude, final double longitude) {
        final ArrayList<InspectionSite> allSites = Data.getInstance().allSites;

        if (!allSites.isEmpty()) {
            return;
        }

        final Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                allSites.clear();
                allSites.addAll(SalesforceJSONHelper.getSites(response));

                for (InspectionSite site : allSites) {
                    if (site.inspections == null) continue;

                    for (Inspection inspection : site.inspections) {
                        getInspectionSteps(context, inspection);
                    }
                }
            }
        };

        final Response.ErrorListener errorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(final VolleyError error) {
                // Detect if this failure is an OAuth failure
                Boolean needsNewToken = false;
                int statusCode = -1;
                if (error.networkResponse != null) {
                    statusCode = error.networkResponse.statusCode;
                }
                needsNewToken = statusCode == 401 || error.getCause().toString().equals("java.io.IOException: No authentication challenges found");
                if (needsNewToken) {
                    Response.Listener<String> tokenResponseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            getGPSCoordinates(context);
                        }
                    };
                    SalesforceAPIManager.getNewAccessToken(context, tokenResponseListener);
                }
            }
        };

        String url = null;
        try {
            String query = String.format("Select Name, Location__c, Id, Address__c, (Select Name, Id, isActive__c, Description__c From Inspections__r) From Inspection_Site__c WHERE DISTANCE(Location__c, GEOLOCATION(%f, %f), 'mi') < %f", latitude, longitude, Constants.DISTANCE_MILES);
            url = SalesforceAPIManager.getInstanceUrl(context) + SalesforceAPIManager.SOQL_QUERY_PATH + URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final SalesforceJSONObjectRequest request = new SalesforceJSONObjectRequest(context, Request.Method.GET, url, null, listener, errorListener);

        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }

    public static void getInspectionSteps(final Activity context, final Inspection inspection) {
        final Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                SalesforceJSONHelper.getInspectionStepsForInspection(inspection, jsonObject);
            }
        };

        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.v(Constants.TAG, volleyError.toString());
            }
        };

        String url = null;
        try {
            String query = String.format("Select isActive__c, Site__c, Name, Id, Description__c, "
                    + "(Select Id, Name, isActive__c, Order__c, Question__c, Required__c, "
                    + "Type__c, Photo__c, Image__c From Inspection_Items__r ORDER BY Order__c) "
                    + "From Inspection__c WHERE Id = '%s'", inspection.id);
            url = getInstanceUrl(context) + SOQL_QUERY_PATH + URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final SalesforceJSONObjectRequest request = new SalesforceJSONObjectRequest(context, Request.Method.GET, url, null, listener, errorListener);

        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }

    public static void getAttachment(final Activity context,
                                     InspectionStep inspectionStep,
                                     Response.Listener<Bitmap> listener,
                                     Response.ErrorListener errorListener) {
        String url = getInstanceUrl(context)
                + "/services/data/v30.0/sobjects/Attachment/"
                + inspectionStep.photoId
                + "/body";
        final SalesforceImageRequest request = new SalesforceImageRequest(context, url, listener, 0, 0, Bitmap.Config.ARGB_8888, errorListener);
        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }


    public static void getDocumentation(final Activity context, final String inspectionStepId, final Response.Listener<JSONObject> listener) {
        String url = null;
        try {
            String query = String.format("Select (Select Video_Link__c From Inspections_Docs__r) From Inspection_Step__c WHERE Id = '%s'", inspectionStepId);
            url = getInstanceUrl(context) + SOQL_QUERY_PATH + URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Response.Listener<String> stringListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.w(Constants.TAG, "Unable to get documentation");
                    }
                };
                getNewAccessToken(context, stringListener);
            }
        };

        final SalesforceJSONObjectRequest request = new SalesforceJSONObjectRequest(context, Request.Method.GET, url, null, listener, errorListener);

        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }


    public static void postNewCase(final Activity context,
                                   final InspectionResponse inspectionResponse,
                                   final String subject,
                                   final String description,
                                   final Response.Listener<JSONObject> listener) {
        String url = SalesforceAPIManager.getInstanceUrl(context) + "/services/data/v30.0/sobjects/Case";
        JSONObject jsonObject = new JSONObject();
        try {
            if (subject != null) {
                jsonObject.put("Subject", subject);
            }
            if (description != null) {
                jsonObject.put("Description", description);
            }
            jsonObject.put("Origin", "Web");
            jsonObject.put("Status", "New");
            jsonObject.put("InspectionResponse__c", inspectionResponse.id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject s) {
                listener.onResponse(s);
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Response.Listener<String> stringListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.w(Constants.TAG, "Unable to post new case");
                    }
                };
                SalesforceAPIManager.getNewAccessToken(context, stringListener);
            }
        };

        SalesforceJSONObjectRequest request = new SalesforceJSONObjectRequest(context, Request.Method.POST, url, jsonObject, jsonObjectListener, errorListener);

        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }

    public static void postInspectionResponse(final Activity context, final InspectionResponse inspectionResponse, final Response.Listener<JSONObject> listener) {
        String url = getInstanceUrl(context) + "/services/data/v30.0/sobjects/Inspection_Response__c";
        JSONObject jsonObject = SalesforceObjectSerializer.serialize(inspectionResponse);
        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject s) {
                listener.onResponse(s);
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.w(Constants.TAG, "Unable to post Inspection Response");
            }
        };

        SalesforceJSONObjectRequest request = new SalesforceJSONObjectRequest(context, Request.Method.POST, url, jsonObject, jsonObjectListener, errorListener);

        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }

    public static void postInspectionStepResponse(final Activity context, final InspectionStepResponse inspectionStepResponse, final Response.Listener<JSONObject> listener) {
        String url = getInstanceUrl(context) + "/services/data/v30.0/sobjects/Inspection_Step_Response__c";
        JSONObject jsonObject = SalesforceObjectSerializer.serialize(inspectionStepResponse);
        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject s) {
                listener.onResponse(s);
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.w(Constants.TAG, "Unable to post InspectionStepResponse");
            }
        };

        SalesforceJSONObjectRequest request = new SalesforceJSONObjectRequest(context, Request.Method.POST, url, jsonObject, jsonObjectListener, errorListener);

        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }

    public static void postAttachment(final Activity context, final String parentId, final String attachmentName, final String encodedImage, final Response.Listener<JSONObject> listener) {
        String url = getInstanceUrl(context) + "/services/data/v30.0/sobjects/Attachment";
        JSONObject jsonObject = new JSONObject();
        try {
            byte[] b;

            jsonObject.put("name", attachmentName);
            jsonObject.put("ParentId", parentId);
            jsonObject.put("body", encodedImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject s) {
                listener.onResponse(s);
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.w(Constants.TAG, "Unable to post attachment for parent " + parentId);
            }
        };

        SalesforceJSONObjectRequest request = new SalesforceJSONObjectRequest(context, Request.Method.POST, url, jsonObject, jsonObjectListener, errorListener);

        VolleySingleton.getInstance(context).getRequestQueue().add(request);
    }
}
