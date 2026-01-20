package edu.uiuc.cs427app;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import edu.uiuc.cs427app.event_data.AppEvent;
import edu.uiuc.cs427app.event_data.EventRepository;


public class MapTabFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.google_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 1. Get the shared events
        List<AppEvent> events = EventRepository.getInstance().getEvents();

        // 2. Geocoding requires a background thread (network operation)
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

            // Loop through all events
            for (AppEvent event : events) {
                try {
                    // Try to find the coordinates for the address string
                    List<Address> addresses = geocoder.getFromLocationName(event.getLocationStr(), 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        // 3. UI updates (adding markers) must happen on the Main Thread
                        getActivity().runOnUiThread(() -> {
                            mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(event.getTitle())
                                    .snippet(event.getLocationStr())); // Shows address when clicked
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Optional: Move camera to the UIUC area after loading
            getActivity().runOnUiThread(() -> {
                LatLng uiuc = new LatLng(40.1020, -88.2272);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uiuc, 14f));
            });

        }).start();
    }
}
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//import java.util.ArrayList;
//import java.util.List;
//
//public class MapTabFragment extends Fragment implements OnMapReadyCallback {
//
//    private GoogleMap mMap;
//
//    // 1. A list to hold our event data
//    private List<SimpleEvent> eventList = new ArrayList<>();
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_map, container, false);
//
//        // 2. Create some dummy events (UIUC Locations)
//        createDummyEvents();
//
//        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
//                .findFragmentById(R.id.google_map);
//        if (mapFragment != null) {
//            mapFragment.getMapAsync(this);
//        }
//        return view;
//    }
//
//    // Helper method to populate data
//    private void createDummyEvents() {
//        eventList.clear();
//        // Illini Union
//        eventList.add(new SimpleEvent("Study Group", 40.1092, -88.2272));
//        // Grainger Library
//        eventList.add(new SimpleEvent("Hackathon Team Meet", 40.1125, -88.2269));
//        // ARC (Activities and Recreation Center)
//        eventList.add(new SimpleEvent("Pickup Basketball", 40.1013, -88.2360));
//        // Krannert Center
//        eventList.add(new SimpleEvent("Music Rehearsal", 40.1079, -88.2235));
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//        // 3. THE LOOP: Iterate through the list and add a marker for each
//        for (SimpleEvent event : eventList) {
//            LatLng location = new LatLng(event.lat, event.lng);
//
//            mMap.addMarker(new MarkerOptions()
//                    .position(location)
//                    .title(event.title)); // The title appears when you tap the pin
//        }
//
//        // 4. Move camera to the first event so we aren't looking at the ocean
//        if (!eventList.isEmpty()) {
//            LatLng firstEvent = new LatLng(eventList.get(0).lat, eventList.get(0).lng);
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstEvent, 14f));
//        }
//    }
//
//    // A simple helper class to store data
//    private static class SimpleEvent {
//        String title;
//        double lat;
//        double lng;
//
//        public SimpleEvent(String title, double lat, double lng) {
//            this.title = title;
//            this.lat = lat;
//            this.lng = lng;
//        }
//    }
//}