package edu.uiuc.cs427app;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private List<String> eventList; // We will just store Event Names for now

    // Constructor: Receives data when we create the adapter
    public EventsAdapter(List<String> eventList) {
        this.eventList = eventList;
    }

    // 1. Create a new row (inflates the XML we made in Step 1)
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    // 2. Fill the row with data (sets the text)
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        String eventName = eventList.get(position);
        holder.titleText.setText(eventName);
        holder.locationText.setText("Campus Location " + (position + 1));
    }

    // 3. Count how many items we have
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    // Helper class to hold the views
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