package edu.uiuc.cs427app.event_data;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import edu.uiuc.cs427app.R;
import edu.uiuc.cs427app.event_data.EventsFragment;
import edu.uiuc.cs427app.event_data.AppEvent;
import edu.uiuc.cs427app.event_data.EventRepository;



public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    // CHANGE: List of AppEvent instead of String
    private List<AppEvent> eventList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AppEvent event); // Pass the whole object
    }

    public EventsAdapter(List<AppEvent> eventList, OnItemClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        AppEvent event = eventList.get(position);
        holder.titleText.setText(event.getTitle());
        // Show the actual address
        holder.locationText.setText(event.getLocationStr());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(event));
    }

    @Override
    public int getItemCount() { return eventList.size(); }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView locationText;

        public EventViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.event_title);
            locationText = itemView.findViewById(R.id.event_location);
        }
    }
}
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import java.util.List;
//
//public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {
//
//    private List<String> eventList;
//    private OnItemClickListener listener; // 1. Add a listener variable
//
//    // 2. Define the Interface
//    public interface OnItemClickListener {
//        void onItemClick(String eventName);
//    }
//
//    // 3. Update Constructor to accept the listener
//    public EventsAdapter(List<String> eventList, OnItemClickListener listener) {
//        this.eventList = eventList;
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_event, parent, false);
//        return new EventViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
//        String eventName = eventList.get(position);
//        holder.titleText.setText(eventName);
//        holder.locationText.setText("Campus Location " + (position + 1));
//
//        // 4. Set the Click Listener on the whole row (itemView)
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                listener.onItemClick(eventName); // Pass the data back to the Fragment
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return eventList.size();
//    }
//
//    public static class EventViewHolder extends RecyclerView.ViewHolder {
//        TextView titleText;
//        TextView locationText;
//
//        public EventViewHolder(View itemView) {
//            super(itemView);
//            titleText = itemView.findViewById(R.id.event_title);
//            locationText = itemView.findViewById(R.id.event_location);
//        }
//    }
//}