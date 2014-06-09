package com.salesforce.glassdemo.api;

import com.salesforce.glassdemo.models.InspectionResponse;
import com.salesforce.glassdemo.models.InspectionStepResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class SalesforceObjectSerializer {

    public static JSONObject serialize(InspectionResponse inspectionResponse) {
        final JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("Inspection__c", inspectionResponse.inspectionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    public static JSONObject serialize(InspectionStepResponse inspectionStepResponse) {
        final JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("Inspection_Response__c", inspectionStepResponse.inspectionResponseId);
            jsonObject.put("Inspection_Item__c", inspectionStepResponse.stepId);
            jsonObject.put("Answer__c", inspectionStepResponse.answer);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
