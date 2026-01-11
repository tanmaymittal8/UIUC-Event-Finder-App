package edu.uiuc.cs427app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "DetailsActivity";

    private Button backButton;
    private WeatherImageGenerator imageGenerator;
    private ImageView cityImageView;
    private ProgressBar cityImageProgress;

    private String cityName;
    private double latitude;
    private double longitude;

    private String apiKey;

    private DatabaseHelper database;
    private User currentUser;

    private WeatherDB weatherData;

    private Handler timeHandler;
    private Runnable timeRunnable;
    private String cityTimezone;
    private TextView cityInfoMessage;
    private WeatherInfo cachedWeather;

    /**
     * Initializes the activity, loads theme, and displays city details.
     * Retrieves city name from intent and sets up UI components.
     *
     * @param savedInstanceState Saved state from previous instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        AuthenticationManager authManager = AuthenticationManager.getInstance(this);

        String username = AuthenticationManager.getInstance(this).getCurrentUser() != null
                ? AuthenticationManager.getInstance(this).getCurrentUser().getUsername()
                : "";
        ThemeSpec spec = ThemeManager.loadForUser(this, username);
        ThemeManager.apply(this, spec);

        backButton = findViewById(R.id.back_button);
        cityImageView = findViewById(R.id.cityImageView);
        cityImageProgress = findViewById(R.id.cityImageProgress);
        backButton.setOnClickListener(v -> finish());
        // Process the Intent payload that has opened this Activity and show the information accordingly
//        String cityName = getIntent().getStringExtra("city");
        database = new DatabaseHelper(this);

        currentUser = authManager.getCurrentUser();
//        load_previous_city_maps();

        cityName = getIntent().getStringExtra("city").toString();
        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);
        apiKey = getIntent().getStringExtra("api_key");

        // Initialize Retrofit
        weatherData = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherDB.class);

        String welcome = "Welcome to " + cityName;

        // Initializing the GUI elements
        TextView welcomeMessage = findViewById(R.id.welcomeText);
        cityInfoMessage = findViewById(R.id.cityInfo);

        // Initialize time handler
        timeHandler = new Handler();

        // Fetch weather data
        weatherData.getWeather(latitude, longitude, apiKey, "hourly", "imperial")
                .enqueue(new Callback<WeatherInfo>() {
                    @Override
                    public void onResponse(Call<WeatherInfo> call, Response<WeatherInfo> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            cachedWeather = response.body();
                            cityTimezone = cachedWeather.timezone;

                            // Start updating time
                            startTimeUpdates();
                            generateCityImageWithWeather(cachedWeather);
                        } else {
                            generateBasicCityImage();
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherInfo> call, Throwable t) {
                        cityInfoMessage.setText("Failed to fetch weather data");
                        Toast.makeText(DetailsActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        welcomeMessage.setText(welcome);
        // Get the weather information from a Service that connects to a weather server and show the results

        imageGenerator = new WeatherImageGenerator();
        String weatherSummary = "current local weather in " + cityName;
        String timeOfDay = inferTimeOfDayLabel();

        generateCityImage(cityName, "", "", weatherSummary, timeOfDay);


        Button buttonMap = findViewById(R.id.mapButton);
        buttonMap.setOnClickListener(this);

        Button weatherInsightsButton = findViewById(R.id.weatherInsightsButton);
        weatherInsightsButton.setOnClickListener(v -> {
            if (cachedWeather != null) {
                Intent intent = new Intent(this, WeatherInsightsActivity.class);
                intent.putExtra("weatherData", formatWeatherData(cachedWeather));
                intent.putExtra("cityName", cityName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Weather data not yet loaded", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Starts periodic time updates for displaying current local time.
     * Updates the weather display every second using a handler.
     */
    private void startTimeUpdates() {
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                updateWeatherDisplay();
                timeHandler.postDelayed(this, 1000); // Update every second
            }
        };
        timeHandler.post(timeRunnable);
    }

    /**
     * Updates the weather display with current time and weather information.
     * Formats time according to city's timezone and displays current weather conditions.
     */
    private void updateWeatherDisplay() {
        if (cachedWeather == null || cityTimezone == null) return;

        Date currentTime = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone(cityTimezone));
        String formattedTime = df.format(currentTime);

        String currentWeather = new StringBuilder()
                .append("Local Time: ")
                .append(formattedTime)
                .append("\nCurrent temperature: ")
                .append(cachedWeather.current.temp)
                .append("°F")
                .append("\nCurrent humidity: ")
                .append(cachedWeather.current.humidity)
                .append("%")
                .append("\nCurrent wind speed: ")
                .append(cachedWeather.current.wind_speed)
                .append(" mph")
                .append("\nCurrent wind direction: ")
                .append(cachedWeather.current.wind_deg)
                .append("°")
                .append("\nCurrent wind gust: ")
                .append(cachedWeather.current.wind_gust)
                .append(" mph")
                .append("\nCurrent weather: ")
                .append(cachedWeather.current.weather[0].description)
                .toString();

        cityInfoMessage.setText(currentWeather);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeHandler != null && timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }


//    public void load_previous_city_maps(){
//        List<City> curr_cities = database.getUserCities(currentUser.getUsername());
//        for (City city : curr_cities) {
//            Intent intent = new Intent(this, MapActivity.class);
//            intent.putExtra("city_name", city.getName());
//            intent.putExtra("latitude", city.getLatitude());
//            intent.putExtra("longitude", city.getLongitude());
//            startActivity(intent);
//
//        }
//    }


    /**
     * Generates a city image asynchronously using weather and location data.
     * Shows loading indicator while generating, then displays the generated image.
     *
     * @param cityName       Name of the city
     * @param stateOrRegion  State or region (optional)
     * @param country        Country name
     * @param weatherSummary Current weather summary
     * @param timeOfDay      Time of day label (daytime/sunset/night)
     */
    private void generateCityImage(
            String cityName,
            String stateOrRegion,
            String country,
            String weatherSummary,
            String timeOfDay
    ) {
        if (imageGenerator == null) {
            return;
        }

        cityImageProgress.setVisibility(View.VISIBLE);
        cityImageView.setImageDrawable(null);

        imageGenerator.generateCityImageAsync(
                cityName,
                stateOrRegion,
                country,
                weatherSummary,
                timeOfDay,
                new WeatherImageGenerator.ImageCallback() {
                    @Override
                    public void onImageReady(@NonNull android.graphics.Bitmap bitmap) {
                        cityImageProgress.setVisibility(View.GONE);
                        cityImageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(@NonNull Throwable t) {
                        cityImageProgress.setVisibility(View.GONE);
                        // Optional: set a fallback image or show a toast
                        // cityImageView.setImageResource(R.drawable.city_placeholder);
                    }
                }
        );
    }

    /**
     * Infers time-of-day label based on current hour.
     * Returns "daytime" (6am-5pm), "sunset" (5pm-9pm), or "night" (9pm-6am).
     *
     * @return Time-of-day label string
     */
    private String inferTimeOfDayLabel() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 6 && hour < 17) {
            return "daytime";
        } else if (hour >= 17 && hour < 21) {
            return "sunset";
        } else {
            return "night";
        }
    }

    /**
     * Handles click events for the map button.
     * TODO: Implement navigation to map activity showing city location.
     *
     * @param view The view that was clicked
     */
    @Override
    public void onClick(View view) {
        //Implement this (create an Intent that goes to a new Activity, which shows the map)
//        double latitude = 40.1164;
//        double longitude = -88.2434;

//        if (latitude == 0.0 && longitude == 0.0){
//            Toast.makeText(this, "City coordinates not available", Toast.LENGTH_SHORT).show();
//            return;
//        }

        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("city_name", cityName);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);
    }

    /**
     * Formats weather data for LLM consumption.
     *
     * @param weather The weather info to format
     * @return Formatted weather data string
     */
    private String formatWeatherData(WeatherInfo weather) {
        StringBuilder sb = new StringBuilder();
        sb.append("Temperature: ").append(weather.current.temp).append("°F\n");
        sb.append("Humidity: ").append(weather.current.humidity).append("%\n");
        sb.append("Wind Speed: ").append(weather.current.wind_speed).append(" mph\n");
        sb.append("Wind Direction: ").append(weather.current.wind_deg).append("°\n");
        if (weather.current.wind_gust > 0) {
            sb.append("Wind Gust: ").append(weather.current.wind_gust).append(" mph\n");
        }
        sb.append("Conditions: ").append(weather.current.weather[0].description);
        return sb.toString();
    }

    //use new prompt to generate image
    private void generateCityImageWithWeather(WeatherInfo weather) {
        if (imageGenerator == null) {
            imageGenerator = new WeatherImageGenerator();
        }

        cityImageProgress.setVisibility(View.VISIBLE);
        cityImageView.setImageDrawable(null);

        imageGenerator.generateCityImageWithWeatherAsync(
                cityName,
                "",
                "",
                weather,
                inferTimeOfDayLabel(),
                new WeatherImageGenerator.ImageCallback() {
                    @Override
                    public void onImageReady(@NonNull Bitmap bitmap) {
                        cityImageProgress.setVisibility(View.GONE);
                        cityImageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(@NonNull Throwable t) {
                        cityImageProgress.setVisibility(View.GONE);
                        generateBasicCityImage();
                    }
                }
        );
    }

    private void generateBasicCityImage() {
        String weatherSummary = "current local weather in " + cityName;
        String timeOfDay = inferTimeOfDayLabel();

        imageGenerator.generateCityImageAsync(
                cityName, "", "", weatherSummary, timeOfDay,
                new WeatherImageGenerator.ImageCallback() {
                    @Override
                    public void onImageReady(@NonNull Bitmap bitmap) {
                        cityImageProgress.setVisibility(View.GONE);
                        cityImageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(@NonNull Throwable t) {
                        cityImageProgress.setVisibility(View.GONE);
                    }
                }
        );
    }
}
