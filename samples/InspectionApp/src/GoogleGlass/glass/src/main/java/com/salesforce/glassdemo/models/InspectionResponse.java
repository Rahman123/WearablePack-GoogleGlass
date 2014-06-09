package com.salesforce.glassdemo.models;

import java.util.ArrayList;

public class InspectionResponse {

    /**
     * The Salesforce Id of this object (returned by API call to create object in Salesforce)
     */
    public String id;

    /**
     * The Salesforce Id of the Inspection object this inspection relates to
     */
    public String inspectionId;

    /**
     * Array of InspectionStepResponse objects
     */
    public ArrayList<InspectionStepResponse> responses;

    public InspectionResponse(Inspection inspection) {
        inspectionId = inspection.id;
        responses = new ArrayList<InspectionStepResponse>();
    }
}
