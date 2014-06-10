package com.salesforce.glassdemo.models;

import java.util.ArrayList;

public class InspectionSite {
    public String id;

    /** Name of the site */
    public String name;

    /**
     * The address of the site
     */
    public String address;

    /**
     * Location in Lat/Long coordinates.
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
