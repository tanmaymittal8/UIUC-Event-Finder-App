package edu.uiuc.cs427app.event_data;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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

import edu.uiuc.cs427app.R;

public class EventDetailFragment extends Fragment implements OnMapReadyCallback {

    private String eventTitle;
    private String eventLocation;
    private String eventDesc;
    private GoogleMap mMap;

    // Standard way to create a fragment with arguments
    public static EventDetailFragment newInstance(String title, String location, String desc) {
        EventDetailFragment fragment = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("location", location);
        args.putString("desc", desc);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventTitle = getArguments().getString("title");
            eventLocation = getArguments().getString("location");
            eventDesc = getArguments().getString("desc");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_detail, container, false);

        // 1. Bind UI Elements
        TextView titleView = view.findViewById(R.id.detail_title);
        TextView locView = view.findViewById(R.id.detail_location);
        TextView descView = view.findViewById(R.id.detail_description);
        Button backBtn = view.findViewById(R.id.btn_back);

        titleView.setText(eventTitle);
        locView.setText(eventLocation);
        descView.setText(eventDesc);

        // 2. Handle Back Button
        backBtn.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // 3. Load the Map Dynamically
        // We use childFragmentManager because we are inside a Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mini_map_container);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.mini_map_container, mapFragment)
                    .commit();
        }
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // 4. Geocode the single location
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(eventLocation, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            mMap.addMarker(new MarkerOptions().position(latLng).title(eventTitle));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}