package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Instrumented test for the weather feature.
 * Verifies that DetailsActivity retrieves and displays up-to-date weather
 * information for two different cities: Chicago and Champaign.
 *
 * This fulfills the role:
 * "Testing the weather feature (collecting up-to-date weather information)
 *  for two cities."
 */
@RunWith(AndroidJUnit4.class)
public class WeatherFeatureTest {
    private static final String OPEN_WEATHER_API_KEY = BuildConfig.WEATHER_API_KEY;

    // -------- City 1 (Chicago) --------
    private static final String CHICAGO_NAME = "Chicago";
    private static final double CHICAGO_LATITUDE = 41.8781;
    private static final double CHICAGO_LONGITUDE = -87.6298;

    // -------- City 2 (Champaign) --------
    private static final String CHAMPAIGN_NAME = "Champaign";
    private static final double CHAMPAIGN_LATITUDE = 40.1164;
    private static final double CHAMPAIGN_LONGITUDE = -88.2434;

    @Before
    public void setUp() {
        // Allow long network calls from OpenWeather API
        IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(60, TimeUnit.SECONDS);
    }

    // -------------------------------------------------------------------------
    // Test 1: Chicago Weather Feature
    // -------------------------------------------------------------------------
    /**
     * Tests weather feature for Chicago.
     * Verifies that DetailsActivity correctly fetches and displays weather data
     * including temperature, humidity, wind speed, and current conditions.
     */
    @Test
    public void testWeatherForChicago() throws InterruptedException {

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), DetailsActivity.class);
        // Put Chicago's information
        intent.putExtra("city", CHICAGO_NAME);
        intent.putExtra("latitude", CHICAGO_LATITUDE);
        intent.putExtra("longitude", CHICAGO_LONGITUDE);
        intent.putExtra("api_key", OPEN_WEATHER_API_KEY);

        ActivityScenario<DetailsActivity> scenario = ActivityScenario.launch(intent);

        // Wait for Retrofit fetch + UI updates
        Thread.sleep(5000);

        // Verify welcome message
        onView(withId(R.id.welcomeText))
                .check(matches(withText("Welcome to " + CHICAGO_NAME)));
        Thread.sleep(500);

        // Verify time
        onView(withId(R.id.cityInfo))
                .check(matches(withText(containsString("Local Time:"))));
        Thread.sleep(500);
        // Verify temperature
        onView(withId(R.id.cityInfo))
                .check(matches(withText(containsString("Current temperature:"))));
        Thread.sleep(500);
        // Verify humidity
        onView(withId(R.id.cityInfo))
                .check(matches(withText(containsString("Current humidity:"))));
        Thread.sleep(500);
        // Verify wind speed
        onView(withId(R.id.cityInfo))
                .check(matches(withText(containsString("Current wind speed:"))));
        Thread.sleep(500);
        // Verify weather
        onView(withId(R.id.cityInfo))
                .check(matches(withText(containsString("Current weather:"))));
        Thread.sleep(500);

        // Ensure no API error occurred
        onView(withId(R.id.cityInfo))
                .check(matches(not(withText(containsString("Failed to fetch weather data")))));

        scenario.close();
    }

    // -------------------------------------------------------------------------
    // Test 2: Champaign Weather Feature
    // -------------------------------------------------------------------------
    /**
     * Tests weather feature for Champaign.
     * Verifies that DetailsActivity correctly fetches and displays weather data
     * including temperature, humidity, wind speed, and current conditions.
     */
    @Test
    public void testWeatherForChampaign() throws InterruptedException {

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), DetailsActivity.class);
        // Put Champaign's information
        intent.putExtra("city", CHAMPAIGN_NAME);
        intent.putExtra("latitude", CHAMPAIGN_LATITUDE);
        intent.putExtra("longitude", CHAMPAIGN_LONGITUDE);
        intent.putExtra("api_key", OPEN_WEATHER_API_KEY);

        ActivityScenario<DetailsActivity> scenario = ActivityScenario.launch(intent);

        Thread.sleep(5000); // Wait for weather fetch

        // Verify welcome text
        onView(withId(R.id.welcomeText))
                .check(matches(withText("Welcome to " + CHAMPAIGN_NAME)));
        Thread.sleep(500);

        // Verify time
        onView(withId(R.id.cityInfo))
                .check(matches(withText(containsString("Local Time:"))));
        Thread.sleep(500);
        // Verify temperature
        onView(withId(R.id.cityInfo))
                .check(matches(withText(containsString("Current temperature:"))));
        Thread.sleep(500);
        // Verify humidity
        onView(withId(R.id.cityInfo))
                .check(matches(withText(containsString("Current humidity:"))));
        Thread.sleep(500);
        // Verify wind speed
        onView(withId(R.id.cityInfo))
                .check(matches(withText(containsString("Current wind speed:"))));
        Thread.sleep(500);
        // Verify weather
        onView(withId(R.id.cityInfo))
                .check(matches(withText(containsString("Current weather:"))));
        Thread.sleep(500);

        // Ensure no error message was triggered
        onView(withId(R.id.cityInfo))
                .check(matches(not(withText(containsString("Failed to fetch weather data")))));

        scenario.close();
    }
}

