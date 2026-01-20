package edu.uiuc.cs427app.event_data;

public class AppEvent {
    String title;
    String locationStr; // e.g., "Illini Union, Urbana, IL"

    public AppEvent(String title, String locationStr) {
        this.title = title;
        this.locationStr = locationStr;
    }

    public String getTitle() { return title; }
    public String getLocationStr() { return locationStr; }
}