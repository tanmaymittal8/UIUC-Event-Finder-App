package edu.uiuc.cs427app; // Changed to match your project package

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

import edu.uiuc.cs427app.event_data.EventsFragment;

public class MapsMarkerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        TabLayout tabLayout = findViewById(R.id.bottomNavBar);

        // Load the Map by default when app starts
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MapTabFragment())
                    .commit();
        }

        // Handle Tab Clicks
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selectedFragment = null;

                // Switch based on the Index (0, 1, 2)
                switch (tab.getPosition()) {
                    case 0:
                        // User clicked "Map"
                        selectedFragment = new MapTabFragment();
                        break;
                    case 1:
                        // User clicked "My Events"
                        selectedFragment = new EventsFragment();
                        break;
                    case 2:
                        // User clicked "Profile"
                        selectedFragment = new ProfileFragment();
                        break;
                }

                // Swap the screen content
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
}

