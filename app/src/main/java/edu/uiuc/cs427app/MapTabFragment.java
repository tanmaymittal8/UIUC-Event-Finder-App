package edu.uiuc.cs427app;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import edu.uiuc.cs427app.event_data.AppEvent;
import edu.uiuc.cs427app.event_data.EventDetailFragment;
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
    public void onResume() {
        super.onResume();
        if (mMap != null) {
            refreshMarkers();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // 1. Set Custom InfoWindow Adapter to use our "Button" layout
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Nullable
            @Override
            public View getInfoContents(@NonNull Marker marker) {
                // Return null to let getInfoWindow handle it, or inflate here
                View v = getLayoutInflater().inflate(R.layout.custom_info_window, null);

                TextView title = v.findViewById(R.id.info_title);
                TextView snippet = v.findViewById(R.id.info_snippet);

                title.setText(marker.getTitle());
                snippet.setText(marker.getSnippet());

                return v;
            }

            @Nullable
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                return null; // Use default frame, but custom contents above
            }
        });

        // 2. Handle the click on the Info Window (The "More Info" button)
        mMap.setOnInfoWindowClickListener(marker -> {
            // Retrieve the AppEvent object we saved in the tag
            AppEvent event = (AppEvent) marker.getTag();

            if (event != null) {
                // Navigate to Detail Fragment
                EventDetailFragment detailFragment = EventDetailFragment.newInstance(
                        event.getTitle(),
                        event.getLocationStr(),
                        event.getDescription()
                );

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(getContext(), "Error loading event details", Toast.LENGTH_SHORT).show();
            }
        });

        refreshMarkers();
    }

    private void refreshMarkers() {
        if (mMap == null) return;

        mMap.clear();
        List<AppEvent> events = EventRepository.getInstance().getEvents();

        new Thread(() -> {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            boolean first = true;

            for (AppEvent event : events) {
                try {
                    List<Address> addresses = geocoder.getFromLocationName(event.getLocationStr(), 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        getActivity().runOnUiThread(() -> {
                            // 3. Add Marker and Attach Data (Tag)
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(event.getTitle())
                                    .snippet(event.getLocationStr()));

                            // This is the critical step: Attach the object to the marker
                            if (marker != null) {
                                marker.setTag(event);
                            }
                        });

                        // Move camera to the first event found
                        if (first) {
                            first = false;
                            getActivity().runOnUiThread(() ->
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f))
                            );
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
//
//import android.location.Address;
//import android.location.Geocoder;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//import java.io.IOException;
//import java.util.List;
//import java.util.Locale;
//import edu.uiuc.cs427app.event_data.AppEvent;
//import edu.uiuc.cs427app.event_data.EventRepository;
//
//
//public class MapTabFragment extends Fragment implements OnMapReadyCallback {
//
//    private GoogleMap mMap;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_map, container, false);
//        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
//                .findFragmentById(R.id.google_map);
//        if (mapFragment != null) {
//            mapFragment.getMapAsync(this);
//        }
//        return view;
//    }
//
//    // In MapTabFragment.java
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        // If the map is ready, reload the markers
//        if (mMap != null) {
//            refreshMarkers();
//        }
//    }
//
//    // Move your marker logic into a helper method
//    private void refreshMarkers() {
//        mMap.clear(); // Clear old markers so we don't get duplicates
//        List<AppEvent> events = EventRepository.getInstance().getEvents();
//
//        new Thread(() -> {
//            // ... (Existing Geocoding logic) ...
//            // ... (runOnUiThread to add markers) ...
//        }).start();
//    }
//
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        mMap.getUiSettings().setZoomControlsEnabled(true);
//        refreshMarkers(); // Initial load
//
//        // 1. Get the shared events
//        List<AppEvent> events = EventRepository.getInstance().getEvents();
//
//        // 2. Geocoding requires a background thread (network operation)
//        new Thread(() -> {
//            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
//
//            // Loop through all events
//            for (AppEvent event : events) {
//                try {
//                    // Try to find the coordinates for the address string
//                    List<Address> addresses = geocoder.getFromLocationName(event.getLocationStr(), 1);
//
//                    if (addresses != null && !addresses.isEmpty()) {
//                        Address address = addresses.get(0);
//                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
//
//                        // 3. UI updates (adding markers) must happen on the Main Thread
//                        getActivity().runOnUiThread(() -> {
//                            mMap.addMarker(new MarkerOptions()
//                                    .position(latLng)
//                                    .title(event.getTitle())
//                                    .snippet(event.getLocationStr())); // Shows address when clicked
//                        });
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            // Optional: Move camera to the UIUC area after loading
//            getActivity().runOnUiThread(() -> {
//                LatLng uiuc = new LatLng(40.1020, -88.2272);
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uiuc, 14f));
//            });
//
//        }).start();
//    }
//}
//


