package com.salesforce.glassdemo.models;

public class InspectionStep {
    public String id;

    /** Main text of the instruction, will be the primary card text. */
    public String text;

    /**
     * Type of the step.
     * Valid values: "yes/no", "pass/fail", "number", "text"
     * String typing isn't the best but it's the easiest for right now.
     */
    public String type;

    /**
     * URL of the image associated with the step.
     * If no image, null or empty.
     */
    public String imageUrl;

    public String photoId;

    public String documentationUrl;

    /**
     * Whether or not the question must be answered
     */
    public Boolean isRequired;

    @Override
    public String toString() {
        return "InspectionStep{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", type='" + type + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", photoId='" + photoId + '\'' +
                ", documentationUrl='" + documentationUrl + '\'' +
                ", isRequired=" + isRequired +
                '}';
    }
}
