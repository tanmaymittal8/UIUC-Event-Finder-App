package edu.uiuc.cs427app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapTabFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize the map fragment found inside the layout
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

        // 1. Define a Location (Latitude, Longitude)
        // Example: UIUC Quad coordinates
        LatLng campusLocation = new LatLng(40.107, -88.227);

        // 2. Add a Marker
        mMap.addMarker(new MarkerOptions()
                .position(campusLocation)
                .title("Campus Center")
                .snippet("Events happening here today!"));

        // 3. Move the Camera to look at this location
        // 15f is the zoom level (1 = World, 15 = Streets, 20 = Buildings)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(campusLocation, 15f));
    }
}