package edu.uiuc.cs427app.event_data;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import edu.uiuc.cs427app.R;

import edu.uiuc.cs427app.event_data.AppEvent;
import edu.uiuc.cs427app.event_data.EventRepository;
import edu.uiuc.cs427app.event_data.EventsAdapter;

public class EventsFragment extends Fragment {

    private EventsAdapter adapter;
    private List<AppEvent> myEvents;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        // 1. Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get live list from Repository
        myEvents = EventRepository.getInstance().getEvents();

        adapter = new EventsAdapter(myEvents, event -> {
            Toast.makeText(getContext(), "Clicked: " + event.getTitle(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        // 2. Setup Floating Action Button
        FloatingActionButton fab = view.findViewById(R.id.fab_add_event);
        fab.setOnClickListener(v -> showAddEventDialog());

        return view;
    }

    private void showAddEventDialog() {
        // Inflate the custom layout for the dialog
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_event, null);

        EditText titleInput = dialogView.findViewById(R.id.edit_event_title);
        EditText descInput = dialogView.findViewById(R.id.edit_event_desc);
        EditText locInput = dialogView.findViewById(R.id.edit_event_location);

        // Build the Alert Dialog
        new AlertDialog.Builder(getContext())
                .setTitle("Add New Event")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = titleInput.getText().toString();
                    String desc = descInput.getText().toString();
                    String location = locInput.getText().toString();

                    if (!title.isEmpty() && !location.isEmpty()) {
                        // 3. Create Event and Add to Repository
                        AppEvent newEvent = new AppEvent(title, location, desc);
                        EventRepository.getInstance().addEvent(newEvent);

                        // 4. Notify Adapter to refresh the list
                        adapter.notifyDataSetChanged();

                        Toast.makeText(getContext(), "Event Added!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Please fill in Title and Location", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast; // Import Toast
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.List;
//
//import edu.uiuc.cs427app.R;
//
//public class EventsFragment extends Fragment {
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_events, container, false);
//
//        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_events);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        // Get data from the Repository
//        List<AppEvent> myEvents = EventRepository.getInstance().getEvents();
//
//        EventsAdapter adapter = new EventsAdapter(myEvents, new EventsAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(AppEvent event) {
//                Toast.makeText(getContext(), "Clicked: " + event.getTitle(), Toast.LENGTH_SHORT).show();
//            }
//        });
//        recyclerView.setAdapter(adapter);
//
////        List<String> myEvents = new ArrayList<>();
////        myEvents.add("Study Group: CS 427");
////        myEvents.add("Hackathon Planning");
////        myEvents.add("Lunch at Green St");
////        myEvents.add("Gym Session");
////        myEvents.add("Project Demo");
////
////        // CHANGE: Pass the listener (lambda) as the second argument
////        EventsAdapter adapter = new EventsAdapter(myEvents, new EventsAdapter.OnItemClickListener() {
////            @Override
////            public void onItemClick(String eventName) {
////                // This code runs when an item is clicked
////                Toast.makeText(getContext(), "Clicked: " + eventName, Toast.LENGTH_SHORT).show();
////
////                // later, you can replace this Toast with code to open a Detail Fragment
////            }
////        });
////
////        recyclerView.setAdapter(adapter);
//
//        return view;
//    }
//}