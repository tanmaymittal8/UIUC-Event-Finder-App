package edu.uiuc.cs427app; // Changed to match your project package

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * An activity that displays a Google map with multiple markers and handles click events.
 */
public class MapsMarkerActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    // 1. Define the location constants
    private final LatLng PERTH = new LatLng(-31.952854, 115.857342);
    private final LatLng SYDNEY = new LatLng(-33.852, 151.211);
    private final LatLng BRISBANE = new LatLng(-27.47093, 153.0235);

    // 2. Define variables to hold the marker objects
    private Marker markerPerth;
    private Marker markerSydney;
    private Marker markerBrisbane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure this layout exists in your res/layout folder (activity_maps.xml)
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // 3. Add markers to the map
        markerPerth = googleMap.addMarker(new MarkerOptions()
                .position(PERTH)
                .title("Perth"));
        markerPerth.setTag(0);

        markerSydney = googleMap.addMarker(new MarkerOptions()
                .position(SYDNEY)
                .title("Sydney"));
        markerSydney.setTag(0);

        markerBrisbane = googleMap.addMarker(new MarkerOptions()
                .position(BRISBANE)
                .title("Brisbane"));
        markerBrisbane.setTag(0);

        // 4. Set the listener for marker clicks
        googleMap.setOnMarkerClickListener(this);

        // Move camera to Sydney
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(SYDNEY));
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        // 5. Retrieve the data (tag) from the marker
        Integer clickCount = (Integer) marker.getTag();

        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);

            Toast.makeText(this,
                    marker.getTitle() + " has been clicked " + clickCount + " times.",
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}