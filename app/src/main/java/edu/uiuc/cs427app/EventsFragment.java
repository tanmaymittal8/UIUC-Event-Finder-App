package edu.uiuc.cs427app;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // Import Toast
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<String> myEvents = new ArrayList<>();
        myEvents.add("Study Group: CS 427");
        myEvents.add("Hackathon Planning");
        myEvents.add("Lunch at Green St");
        myEvents.add("Gym Session");
        myEvents.add("Project Demo");

        // CHANGE: Pass the listener (lambda) as the second argument
        EventsAdapter adapter = new EventsAdapter(myEvents, new EventsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String eventName) {
                // This code runs when an item is clicked
                Toast.makeText(getContext(), "Clicked: " + eventName, Toast.LENGTH_SHORT).show();

                // later, you can replace this Toast with code to open a Detail Fragment
            }
        });

        recyclerView.setAdapter(adapter);

        return view;
    }
}