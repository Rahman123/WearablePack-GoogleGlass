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
        } else {
            setFootnote(step.type);
        }

        if (step.imageUrl != null && !step.imageUrl.isEmpty()) {
            // If there is an image, set the card image whenever the image is finish downloading
            new SetCardImageTask(this).execute(step.imageUrl);
        }
    }

    protected StepCard(Context context) {
        super(context);
    }
}
