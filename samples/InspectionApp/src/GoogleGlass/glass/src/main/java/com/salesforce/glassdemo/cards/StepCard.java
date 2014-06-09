package com.salesforce.glassdemo.cards;

import android.content.Context;

import com.google.android.glass.app.Card;
import com.salesforce.glassdemo.Constants;
import com.salesforce.glassdemo.models.InspectionStep;
import com.salesforce.glassdemo.util.SetCardImageTask;

public class StepCard extends Card {
    protected InspectionStep step;

    public StepCard(Context context, InspectionStep step) {
        this(context);
        this.step = step;
        setText(step.text);

        if (step.type.equals(Constants.InspectionTypes.TYPE_NUMBER) || step.type.equals(Constants.InspectionTypes.TYPE_TEXT)) {
            setFootnote(step.type + " - Say dictate to input data");
        } else if (step.type.equals(Constants.InspectionTypes.TYPE_AFFIRMATIVE_NEGATIVE)) {
            setFootnote("Affirmative/Negative");
        } else if (step.type.equals(Constants.InspectionTypes.TYPE_SUCCESS_FAILURE)) {
            setFootnote("Success/Failure");
        } else {
            setFootnote(step.type);
        }

        if (step.imageUrl != null && !step.imageUrl.isEmpty()) {
            new SetCardImageTask(this).execute(step.imageUrl);
        }
    }

    protected StepCard(Context context) {
        super(context);
    }
}
