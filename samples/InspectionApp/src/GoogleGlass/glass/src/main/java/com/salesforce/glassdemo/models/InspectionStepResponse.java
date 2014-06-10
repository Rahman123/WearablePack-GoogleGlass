package com.salesforce.glassdemo.models;

import android.util.Log;

import com.salesforce.glassdemo.Constants;

public class InspectionStepResponse {
    /**
     * The Salesforce Id of this object (returned by POST call to add this object to Salesforce)
     */
    public String id;

    /**
     * The Salesforce Id of the Inspection Response object this step response belongs to
     */
    public String inspectionResponseId;

    /**
     * The Salesforce Id of the Inspection Ste object this is a response to
     */
    public String stepId;

    /**
     * The answer to the question from the InspectionStep
     */
    public String answer;

    public String photo;
    public boolean hasPhoto;

    public InspectionStepResponse(InspectionStep step, InspectionResponse response, String answer) {
        this.inspectionResponseId = response.id;
        this.stepId = step.id;
        this.answer = answer;
        Log.i(Constants.TAG, "New ISR with " + stepId);
    }

    @Override
    public String toString() {
        return "InspectionStepResponse{" +
                "id='" + id + '\'' +
                ", inspectionResponseId='" + inspectionResponseId + '\'' +
                ", stepId='" + stepId + '\'' +
                ", answer='" + answer + '\'' +
                ", photo='" + photo + '\'' +
                ", hasPhoto=" + hasPhoto +
                '}';
    }
}
