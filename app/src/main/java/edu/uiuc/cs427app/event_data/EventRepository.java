package edu.uiuc.cs427app.event_data;

import java.util.ArrayList;
import java.util.List;

public class EventRepository {
    private static EventRepository instance;
    private List<AppEvent> events;

    private EventRepository() {
        events = new ArrayList<>();
        // Use Specific Addresses for Geocoding to work best!
        events.add(new AppEvent("Study Group: CS 427", "Siebel Center for Computer Science, Urbana, IL", "test"));
        events.add(new AppEvent("Hackathon Planning", "Grainger Engineering Library, Urbana, IL", "test"));
        events.add(new AppEvent("Lunch at Green St", "Mia Za's, Green St, Champaign, IL", "test"));
        events.add(new AppEvent("Gym Session", "ARC, Peabody Dr, Champaign, IL", "test"));
        events.add(new AppEvent("Project Demo", "Illini Union, Urbana, IL", "test"));
    }

    public static synchronized EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
        }
        return instance;
    }

    public void addEvent(AppEvent event) {
        events.add(event);
    }

    public List<AppEvent> getEvents() {
        return events;
    }
}