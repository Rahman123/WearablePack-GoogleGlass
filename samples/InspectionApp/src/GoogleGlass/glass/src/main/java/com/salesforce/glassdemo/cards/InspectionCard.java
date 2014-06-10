package com.salesforce.glassdemo.cards;

import android.content.Context;
import android.util.Log;

import com.google.android.glass.app.Card;
import com.salesforce.glassdemo.Constants;
import com.salesforce.glassdemo.models.Inspection;

public class InspectionCard extends Card {
    public Inspection inspection;

    public InspectionCard(Context context, Inspection inspection) {
        this(context);
        this.inspection = inspection;
        setText(inspection.title);
        if (inspection.steps != null && !inspection.steps.isEmpty()) {
            setFootnote(inspection.steps.size() + " steps");
        } else {
            Log.e(Constants.TAG, "Inspection has no steps");
            setFootnote("No steps");
        }
    }

    protected InspectionCard(Context context) {
        super(context);
    }
}
