package com.inmo.arsdksample.model;

import java.util.Date;

/**
 * Model class representing a dating interaction recorded by the app
 */
public class Interaction {
    private long id;
    private Date timestamp;
    private String detectedEmotion;
    private String pickupLine;
    private boolean wasSuccessful;
    private String notes;
    private String imageFilePath; // Local path to saved image

    public Interaction() {
        this.timestamp = new Date();
        this.wasSuccessful = false;
    }

    public Interaction(String detectedEmotion, String pickupLine, String imageFilePath) {
        this();
        this.detectedEmotion = detectedEmotion;
        this.pickupLine = pickupLine;
        this.imageFilePath = imageFilePath;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetectedEmotion() {
        return detectedEmotion;
    }

    public void setDetectedEmotion(String detectedEmotion) {
        this.detectedEmotion = detectedEmotion;
    }

    public String getPickupLine() {
        return pickupLine;
    }

    public void setPickupLine(String pickupLine) {
        this.pickupLine = pickupLine;
    }

    public boolean isWasSuccessful() {
        return wasSuccessful;
    }

    public void setWasSuccessful(boolean wasSuccessful) {
        this.wasSuccessful = wasSuccessful;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }
}