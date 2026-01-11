package edu.uiuc.cs427app;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Activity for displaying a city's location on an embedded Google Maps view.
 * Shows city name, coordinates, and an interactive map using WebView.
 */
public class MapActivity extends AppCompatActivity {

    private TextView cityNameTextView;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private WebView mapWebView;
    private ProgressBar loadingProgressBar;
    private Button backButton;

    private String cityName;
    private double latitude;
    private double longitude;

    private AuthenticationManager authManager;

    /**
     * Initializes the activity, loads theme, retrieves city data, and sets up the map.
     *
     * @param savedInstanceState Saved state from previous instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        authManager = AuthenticationManager.getInstance(this);
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initializeViews();

        if (!getCityDataFromIntent()) {
            showErrorAndFinish("Error: Missing city data. Please try again.");
            return;
        }

        displayCityInfo();
        setupMap();
        setupBackButton();
    }

    /**
     * Applies the current user's theme preference to the activity.
     */
    private void applyTheme() {
        Theme theme = Theme.LIGHT;
        if (authManager.getCurrentUser() != null) {
            theme = authManager.getCurrentUser().getTheme();
        }
        int mode = (theme == Theme.DARK)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    /**
     * Initializes all UI components by finding views by their IDs.
     */
    private void initializeViews() {
        cityNameTextView = findViewById(R.id.cityNameTextView);
        latitudeTextView = findViewById(R.id.latitudeTextView);
        longitudeTextView = findViewById(R.id.longitudeTextView);
        mapWebView = findViewById(R.id.mapWebView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        backButton = findViewById(R.id.backButton);
    }

    /**
     * Retrieves city data (name, latitude, longitude) from the intent extras.
     *
     * @return true if all required data is present and valid, false otherwise
     */
    private boolean getCityDataFromIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) return false;

        cityName = extras.getString("city_name");
        if (cityName == null || cityName.trim().isEmpty()) return false;

        if (!extras.containsKey("latitude") || !extras.containsKey("longitude")) {
            return false;
        }

        latitude = extras.getDouble("latitude", 0.0);
        longitude = extras.getDouble("longitude", 0.0);

        return isValidCoordinate(latitude, longitude);
    }

    /**
     * Validates that coordinates are within valid geographic ranges.
     *
     * @param lat Latitude to validate (-90 to 90)
     * @param lon Longitude to validate (-180 to 180)
     * @return true if coordinates are valid, false otherwise
     */
    private boolean isValidCoordinate(double lat, double lon) {
        return lat >= -90.0 && lat <= 90.0 && lon >= -180.0 && lon <= 180.0;
    }

    /**
     * Displays city name and coordinates in the UI text views.
     */
    private void displayCityInfo() {
        cityNameTextView.setText(cityName);
        latitudeTextView.setText(String.format("Latitude: %.6f", latitude));
        longitudeTextView.setText(String.format("Longitude: %.6f", longitude));
    }

    /**
     * Configures the WebView to display Google Maps with the city's location.
     * Enables JavaScript, zoom controls, and handles loading states.
     */
    private void setupMap() {
        WebSettings webSettings = mapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);

        mapWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                loadingProgressBar.setVisibility(View.GONE);
                mapWebView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(MapActivity.this,
                        "Failed to load map: " + description,
                        Toast.LENGTH_LONG).show();
            }
        });

        String mapUrl = buildMapUrl(latitude, longitude);
        String htmlContent = buildMapHtml(mapUrl);
        mapWebView.loadData(htmlContent, "text/html", "UTF-8");
    }

    /**
     * Builds the Google Maps embed URL for the given coordinates.
     *
     * @param lat Latitude coordinate
     * @param lon Longitude coordinate
     * @return Google Maps embed URL string
     */
    private String buildMapUrl(double lat, double lon) {
        return "https://maps.google.com/maps?q=" + lat + "," + lon +
                "&t=&z=15&ie=UTF8&iwloc=&output=embed";
    }

    /**
     * Builds HTML content to embed the Google Maps iframe.
     *
     * @param mapUrl The Google Maps URL to embed
     * @return Complete HTML string with responsive iframe
     */
    private String buildMapHtml(String mapUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { margin: 0; padding: 0; }" +
                "iframe { width: 100%; height: 100vh; border: 0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<iframe src='" + mapUrl + "' allowfullscreen></iframe>" +
                "</body>" +
                "</html>";
    }

    /**
     * Sets up the back button click listener to finish the activity.
     */
    private void setupBackButton() {
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Displays an error message and closes the activity.
     *
     * @param message Error message to display
     */
    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * Cleans up WebView resources when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapWebView != null) {
            mapWebView.clearHistory();
            mapWebView.clearCache(true);
            mapWebView.loadUrl("about:blank");
            mapWebView.onPause();
            mapWebView.removeAllViews();
            mapWebView.destroyDrawingCache();
            mapWebView.destroy();
            mapWebView = null;
        }
    }

    /**
     * Handles back button press to navigate WebView history or close activity.
     */
    @Override
    public void onBackPressed() {
        if (mapWebView != null && mapWebView.canGoBack()) {
            mapWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}