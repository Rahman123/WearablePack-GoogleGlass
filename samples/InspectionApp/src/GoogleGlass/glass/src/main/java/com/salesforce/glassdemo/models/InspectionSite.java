package com.salesforce.glassdemo.models;

import java.util.ArrayList;

public class InspectionSite {
    // TODO need to keep track of Salesforce-provided ID. Assuming string
    public String id;

    /** Name of the site */
    public String name;

    /**
     * The address of the site
     * TODO:
     * This is only here to give some sort of subtitle, assuming that we'll
     * want to display it at some point. I'm not sure that we have something
     * to put here or that it exists in the data model serverside.
     * Obviously, open to changing this and needs to be revisited.
     */
    public String address;

    /**
     * Location in Lat/Long coordinates.
     * TODO: verify this is how data should be represented
     */
    public double lat, lng;

    /** List of inspections */
    public ArrayList<Inspection> inspections;

    public InspectionSite() {
        inspections = new ArrayList<Inspection>();
    }

    @Override
    public String toString() {
        return "InspectionSite{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", inspections=" + inspections +
                '}';
    }
}
