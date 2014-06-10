package com.salesforce.glassdemo.cards;

import android.content.Context;

import com.google.android.glass.app.Card;
import com.salesforce.glassdemo.models.InspectionSite;

public class SiteCard extends Card {
    public InspectionSite site;

    public SiteCard(Context context, InspectionSite site) {
        this(context);
        this.site = site;
        setText(site.name);

        if (site.inspections == null || site.inspections.isEmpty()) {
            setFootnote("No inspections");
        } else {
            setFootnote(site.address);
        }
    }

    protected SiteCard(Context context) {
        super(context);
    }
}
