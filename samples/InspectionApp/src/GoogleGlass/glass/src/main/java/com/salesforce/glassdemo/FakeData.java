package com.salesforce.glassdemo;

import com.salesforce.glassdemo.models.Inspection;
import com.salesforce.glassdemo.models.InspectionSite;
import com.salesforce.glassdemo.models.InspectionStep;

import java.util.ArrayList;

public class FakeData {
    public static InspectionSite getTestSite() {
        InspectionSite inspectionSite = new InspectionSite();
        inspectionSite.name = "Moscone Center";

        inspectionSite.inspections = new ArrayList<Inspection>();

        Inspection inspection = new Inspection();
        inspection.id = "-1";
        inspection.title = "Test Inspection";
        inspection.steps = getFakeSteps();

        inspectionSite.inspections.add(inspection);

        return inspectionSite;
    }

    public static ArrayList<InspectionStep> getFakeSteps() {
        ArrayList<InspectionStep> steps = new ArrayList<InspectionStep>();
        for (int i = 0; i < 5; ++i) {
            InspectionStep step = new InspectionStep();
            step.id = Integer.toString(i);
            step.text = "Step " + i;
            step.subtitle = "Subtitle " + i;
            step.type = "yes/no";
            steps.add(step);
        }
        return steps;
    }
}
