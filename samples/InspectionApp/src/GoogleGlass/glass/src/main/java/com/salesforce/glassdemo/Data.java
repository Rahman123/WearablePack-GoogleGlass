package com.salesforce.glassdemo;

import android.util.Log;

import com.salesforce.glassdemo.models.Inspection;
import com.salesforce.glassdemo.models.InspectionSite;

import java.util.ArrayList;

public class Data {
    private static Data data;

    public ArrayList<InspectionSite> allSites = new ArrayList<InspectionSite>();

    private Data() {
    }

    public static Data getInstance() {
        if (data == null) data = new Data();
        return data;
    }

    public InspectionSite getSiteWithId(String siteId) {
        for (InspectionSite site : allSites) {
            if (site.id.equals(siteId)) {
                return site;
            }
        }
        return null;
    }

    public Inspection getInspectionFromSite(String siteId, String inspectionId) {
        InspectionSite site = getSiteWithId(siteId);
        if (site.inspections == null || site.inspections.isEmpty()) {
            Log.e(Constants.TAG, "Site " + siteId + " has no inspections to search");
            return null;
        }

        for (Inspection inspection : site.inspections) {
            if (inspection.id.equals(inspectionId)) {
                return inspection;
            }
        }

        Log.e(Constants.TAG, "Site " + siteId + " contains no inspection with id " + inspectionId);
        return null;
    }
}
