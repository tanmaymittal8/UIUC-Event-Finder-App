package edu.uiuc.cs427app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.ui.AppBarConfiguration;

import java.util.ArrayList;
import java.util.List;

import edu.uiuc.cs427app.databinding.ActivityMainBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String API_KEY = BuildConfig.WEATHER_API_KEY;
    private final ArrayList<String> cities = new ArrayList<>();
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private AuthenticationManager authManager;
    private LocationDB locationData;
    private DatabaseHelper database;
    private User currentUser;
    private String display_city_map;


    /**
     * Applies the specified theme to the activity.
     *
     * @param t The theme to apply (LIGHT or DARK)
     */
    private void applyTheme(Theme t) {
        int mode = (t == Theme.DARK)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    /**
     * Initializes the activity, checks authentication, loads user cities, and sets up UI.
     * Redirects to login if user is not authenticated.
     *
     * @param savedInstanceState Saved state from previous instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        authManager = AuthenticationManager.getInstance(this);
        Theme t = Theme.LIGHT;

        if (authManager.getCurrentUser() != null) {
            t = authManager.getCurrentUser().getTheme();
        }
        applyTheme(t);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize AuthenticationManager
        currentUser = authManager.getCurrentUser();
        database = new DatabaseHelper(this);

        // Check if user is logged in, if not redirect to login
        if (!authManager.isLoggedIn() || currentUser == null) {
            navigateToLogin();
            return;
        }

        String usernameFromIntent = getIntent().getStringExtra("username");
        String username = (usernameFromIntent != null && !usernameFromIntent.isEmpty())
                ? usernameFromIntent
                : (authManager.getCurrentUser() != null ? authManager.getCurrentUser().getUsername() : "");

        // Load saved ThemeSpec (DB first, SP fallback) and apply to views
        ThemeSpec spec = ThemeManager.loadForUser(this, username != null ? username : "");
        ThemeManager.apply(this, spec);
        // Display username in header
        TextView userHeaderTextView = findViewById(R.id.userHeaderTextView);

        if (currentUser != null) {
            userHeaderTextView.setText("Team 415 - " + currentUser.getUsername());
        }

        Button buttonNew = findViewById(R.id.buttonAddLocation);
        Button logoutButton = findViewById(R.id.logoutButton);
        Button testMapButton = findViewById(R.id.testMapButton);


        buttonNew.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
        testMapButton.setOnClickListener(this);


        loadUserCities();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        locationData = retrofit.create(LocationDB.class);
    }

    /**
     * Loads and displays all cities associated with the current user.
     * Retrieves cities from database and adds them to the UI.
     */
    private void loadUserCities() {

        LinearLayout display_cities = findViewById(R.id.userCityView);

        List<City> curr_cities = database.getUserCities(currentUser.getUsername());
        Log.d("MainActivity", "Loading " + curr_cities.size() + " cities");

        for (City city : curr_cities) {
            Log.d("MainActivity", "City: " + city.getName() +
                    ", ID: " + city.getCityId() +
                    ", Lat: " + city.getLatitude() +
                    ", Lon: " + city.getLongitude());
            addCity(city);
        }
    }

    /**
     * Handles click events for add location and logout buttons.
     *
     * @param view The view that was clicked
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent intent;

        if (id == R.id.buttonAddLocation) {
            promptCity();
        } else if (id == R.id.logoutButton) {
            authManager.logout();
            navigateToLogin();
        } else if (id == R.id.testMapButton){
            intent = new Intent(this, MapsMarkerActivity.class);
            startActivity(intent);
        }


    }

    /**
     * Navigates to the LoginActivity.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // Prevent going back to main activity with back button
    }

    /**
     * Displays a dialog to prompt user for city name input.
     * Fetches location options from weather API and allows user to select from results.
     */
    private void promptCity() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a new city");
        final EditText cityInput = new EditText(this);
        cityInput.setId(R.id.searchCityInput);
        builder.setView(cityInput);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String cityName = cityInput.getText().toString().trim();
                if (!cityName.isEmpty()) {
                    Call<ArrayList<LocationInfo>> call = locationData.getLocationOptions(cityName, 5, API_KEY);
                    call.enqueue(new Callback<ArrayList<LocationInfo>>() {
                        @Override
                        public void onResponse(Call<ArrayList<LocationInfo>> call, Response<ArrayList<LocationInfo>> response) {
                            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                ArrayList<LocationInfo> locOptions = response.body();
                                String[] loc = new String[locOptions.size()];
                                for (int i = 0; i < locOptions.size(); i++) {
                                    loc[i] = locOptions.get(i).location();
                                }

                                AlertDialog.Builder select = new AlertDialog.Builder(MainActivity.this);
                                select.setTitle("Select a City");
                                select.setItems(loc, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogNested, int idNested) {
                                        LocationInfo userChoice = locOptions.get(idNested);
                                        String cityToDisplay = userChoice.location();
                                        if (!cities.contains(cityToDisplay)) {

                                            double lat = userChoice.getLat();
                                            double lon = userChoice.getLon();


                                            City adding_user_new_city = new City(cityToDisplay, lat, lon, "test", "test");

                                            long long_cityID = database.insertCity(adding_user_new_city);
                                            int cityID = (int) long_cityID;

                                            if (cityID == -1) {
                                                showInvalid("this city is not getting added to the database");
                                                return;
                                            }
                                            adding_user_new_city.setCityId(cityID);
                                            Log.d("MainActivity", "City added: " + adding_user_new_city.getName() + " with ID: " + cityID);

                                            if (database.addCityToUser(currentUser.getUsername(), cityID)) {
                                                addCity(adding_user_new_city);
                                                addMap(adding_user_new_city);
                                                display_city_map = adding_user_new_city.getName();
                                            } else {
                                                showInvalid("this city is not getting added to the database");
                                            }
//                                            addCity(cityToDisplay);
                                        }
                                    }
                                });

                                select.show();

                            } else {
                                showInvalid(cityName);
                            }
                        }

                        @Override
                        public void onFailure(Call<ArrayList<LocationInfo>> call, Throwable t) {
                            showInvalid(cityName);
                        }
                    });
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * Placeholder method for adding map functionality for a city.
     *
     * @param cityName The city to add map for
     */
    private void addMap(City cityName) {

    }

    /**
     * Adds a city to the UI display with weather and remove buttons.
     * Creates a horizontal layout with city name, show weather button, and remove button.
     *
     * @param cityName The City object to add to the display
     */
    private void addCity(City cityName) {
        for (String city : cities) {
            if (city.equalsIgnoreCase(cityName.getName())) {
                return;
            }
        }
        cities.add(cityName.getName());
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView cityNameDisplay = new TextView(this);
        cityNameDisplay.setText(cityName.getName().toUpperCase());
        cityNameDisplay.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button showDetails = new Button(this);
        showDetails.setText("Show Weather");
        showDetails.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        showDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("city", cityName.getName());
                intent.putExtra("latitude", cityName.getLatitude()); // temp hardcoding
                intent.putExtra("longitude", cityName.getLongitude()); // temp hardcoding
                intent.putExtra("api_key", API_KEY);
                startActivity(intent);
            }
        });

        Button Remove_city = new Button(this);
        Remove_city.setText("Remove");
        Remove_city.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        Remove_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LinearLayout listCities = findViewById(R.id.userCityView);
                listCities.removeView(layout);
                cities.remove(cityName.getName());
                database.removeCityFromUser(currentUser.getUsername(), cityName.getCityId());
            }
        });


        layout.addView(cityNameDisplay);
        layout.addView(showDetails);
        layout.addView(Remove_city);


        LinearLayout listCities = findViewById(R.id.userCityView);
        listCities.addView(layout);
    }

    /**
     * Displays an error dialog for invalid city names.
     *
     * @param cityName The invalid city name to display in the error message
     */
    private void showInvalid(String cityName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invalid City");
        builder.setMessage("'" + cityName + "' is not a valid city.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
