package com.salesforce.glassdemo.api;

import android.util.Log;

import com.salesforce.glassdemo.Constants;
import com.salesforce.glassdemo.models.Inspection;
import com.salesforce.glassdemo.models.InspectionSite;
import com.salesforce.glassdemo.models.InspectionStep;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SalesforceJSONHelper {

    public static List<InspectionSite> getSites(JSONObject response) {
        final ArrayList<InspectionSite> allSites = new ArrayList<InspectionSite>();

        try {
            JSONArray records = response.getJSONArray(Constants.RECORDS);
            for (int i = 0; i < records.length(); i++) {
                JSONObject aRecord = records.getJSONObject(i);
                InspectionSite site = new InspectionSite();
                site.address = aRecord.getString(Constants.ADDRESS__C);
                site.id = aRecord.getString(Constants.ID);
                site.name = aRecord.getString(Constants.NAME);
                JSONObject location = aRecord.getJSONObject(Constants.LOCATION__C);
                site.lat = location.getDouble(Constants.LATITUDE);
                site.lng = location.getDouble(Constants.LONGITUDE);
                if (!aRecord.getString(Constants.INSPECTIONS__R).equals(Constants.NULL)) {
                    JSONArray inspections = aRecord.getJSONObject(Constants.INSPECTIONS_R).getJSONArray(Constants.RECORDS);
                    for (int j = 0; j < inspections.length(); j++) {
                        JSONObject inspectionJSON = inspections.getJSONObject(j);
                        Inspection inspection = new Inspection();
                        inspection.title = inspectionJSON.getString(Constants.NAME);
                        inspection.id = inspectionJSON.getString(Constants.ID);
                        site.inspections.add(inspection);
                    }
                }
                allSites.add(site);
            }
            Log.v(Constants.TAG, "JSON parsing complete. Total sites: " + allSites.size());
            for (InspectionSite aSite : allSites) {
                Log.v(Constants.TAG, " - " + aSite.toString());
                for (Inspection anInspection : aSite.inspections) {
                    Log.v(Constants.TAG, " -- " + anInspection.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return allSites;
    }

    /**
     * Extract step IDs from inspectionJson and put into inspection.
     * Will not extract attachments from each step
     *
     * @param inspection     Inspection model to save steps into
     * @param inspectionJson JSON to pull steps out of
     */
    public static void getInspectionStepsForInspection(Inspection inspection, JSONObject inspectionJson) {
        try {
            JSONArray records = inspectionJson.getJSONArray(Constants.RECORDS);
            if (records.length() > 0) {
                JSONObject inspectionItem = records.getJSONObject(0);
                if (inspectionItem.has(Constants.INSPECTION_ITEMS__R) && inspectionItem.isNull(Constants.INSPECTION_ITEMS__R)) {
                    // no inspection steps found
                    inspection.steps.clear();
                    return;
                }

                JSONObject inspectionItemsJson = inspectionItem.getJSONObject(Constants.INSPECTION_ITEMS__R);
                JSONArray inspectionStepsJSON = inspectionItemsJson.getJSONArray(Constants.RECORDS);

                inspection.steps.clear();
                for (int i = 0; i < inspectionStepsJSON.length(); i++) {
                    JSONObject stepJSON = inspectionStepsJSON.getJSONObject(i);
                    InspectionStep step = new InspectionStep();
                    step.text = stepJSON.getString(Constants.QUESTION__C);
                    step.id = stepJSON.getString(Constants.ID);
                    step.type = stepJSON.getString(Constants.TYPE__C);
                    step.isRequired = stepJSON.getBoolean(Constants.REQUIRED__C);
                    if (!stepJSON.getString(Constants.PHOTO__C).equals(Constants.NULL)) {
                        step.photoId = stepJSON.getString(Constants.PHOTO__C);
                    }
                    inspection.steps.add(step);
                    Log.i(Constants.TAG, "Step: " + step);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
