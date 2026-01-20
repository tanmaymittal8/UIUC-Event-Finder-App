package edu.uiuc.cs427app.event_data;
public class AppEvent {
    String title;
    String locationStr;
    String description; // New Field

    public AppEvent(String title, String locationStr, String description) {
        this.title = title;
        this.locationStr = locationStr;
        this.description = description;
    }

    // Getters
    public String getTitle() { return title; }
    public String getLocationStr() { return locationStr; }
    public String getDescription() { return description; }
}