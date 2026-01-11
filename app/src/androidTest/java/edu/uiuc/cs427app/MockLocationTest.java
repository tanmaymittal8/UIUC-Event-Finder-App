package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

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
 * Instrumented test for mocking location functionality.
 * This test demonstrates mocking location by simulating a location change
 * from Chicago to Champaign and verifying the map displays the correct coordinates.
 */
@RunWith(AndroidJUnit4.class)
public class MockLocationTest {

    // Chicago coordinates
    private static final String CHICAGO_NAME = "Chicago";
    private static final double CHICAGO_LATITUDE = 41.8781;
    private static final double CHICAGO_LONGITUDE = -87.6298;

    // Champaign coordinates (mocked location)
    private static final String CHAMPAIGN_NAME = "Champaign";
    private static final double CHAMPAIGN_LATITUDE = 40.1164;
    private static final double CHAMPAIGN_LONGITUDE = -88.2434;

    @Before
    public void setUp() {
        // Increase Espresso timeout for slower operations
        IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(60, TimeUnit.SECONDS);
    }

    /**
     * Test that verifies mocking location functionality.
     * Steps:
     * 1. First displays map for Chicago
     * 2. Then mocks the location to Champaign
     * 3. Asserts that the displayed coordinates match Champaign (mocked location)
     */
    @Test
    public void testMockLocationFromChicagoToChampaign() throws InterruptedException {
        // Step 1: Launch MapActivity with Chicago coordinates
        Intent chicagoIntent = new Intent(ApplicationProvider.getApplicationContext(), MapActivity.class);
        chicagoIntent.putExtra("city_name", CHICAGO_NAME);
        chicagoIntent.putExtra("latitude", CHICAGO_LATITUDE);
        chicagoIntent.putExtra("longitude", CHICAGO_LONGITUDE);

        ActivityScenario<MapActivity> chicagoScenario = ActivityScenario.launch(chicagoIntent);

        // Allow time for activity to fully initialize and gain focus
        Thread.sleep(3000);

        // Verify Chicago is displayed (using onActivity to ensure activity is ready)
        chicagoScenario.onActivity(activity -> {
            // Activity is now guaranteed to be resumed
        });

        Thread.sleep(500);

        onView(withId(R.id.cityNameTextView))
                .check(matches(withText(CHICAGO_NAME)));

        onView(withId(R.id.latitudeTextView))
                .check(matches(withText(containsString("41.878"))));

        onView(withId(R.id.longitudeTextView))
                .check(matches(withText(containsString("-87.629"))));

        Thread.sleep(1500);

        // Close Chicago map
        chicagoScenario.close();

        Thread.sleep(1500);

        // Step 2: Mock location by launching MapActivity with Champaign coordinates
        // This simulates mocking the location to a different city
        Intent champaignIntent = new Intent(ApplicationProvider.getApplicationContext(), MapActivity.class);
        champaignIntent.putExtra("city_name", CHAMPAIGN_NAME);
        champaignIntent.putExtra("latitude", CHAMPAIGN_LATITUDE);
        champaignIntent.putExtra("longitude", CHAMPAIGN_LONGITUDE);

        ActivityScenario<MapActivity> champaignScenario = ActivityScenario.launch(champaignIntent);

        // Allow time for activity to fully initialize and gain focus
        Thread.sleep(3000);

        // Ensure activity is ready
        champaignScenario.onActivity(activity -> {
            // Activity is now guaranteed to be resumed
        });

        Thread.sleep(500);

        // Step 3: Assert that the mocked location (Champaign) is now displayed
        // Verify city name shows Champaign
        onView(withId(R.id.cityNameTextView))
                .check(matches(withText(CHAMPAIGN_NAME)));

        // Verify latitude shows Champaign's latitude (40.1164)
        onView(withId(R.id.latitudeTextView))
                .check(matches(withText(containsString("40.116"))));

        // Verify longitude shows Champaign's longitude (-88.2434)
        onView(withId(R.id.longitudeTextView))
                .check(matches(withText(containsString("-88.243"))));

        // Cleanup
        champaignScenario.close();
    }

    /**
     * Test that verifies the map correctly displays mocked coordinates.
     * This test directly mocks a location and asserts the map shows correct info.
     */
    @Test
    public void testMockLocationDisplaysChampaignCoordinates() throws InterruptedException {
        // Mock location: Champaign, IL
        Intent mockLocationIntent = new Intent(ApplicationProvider.getApplicationContext(), MapActivity.class);
        mockLocationIntent.putExtra("city_name", CHAMPAIGN_NAME);
        mockLocationIntent.putExtra("latitude", CHAMPAIGN_LATITUDE);
        mockLocationIntent.putExtra("longitude", CHAMPAIGN_LONGITUDE);

        ActivityScenario<MapActivity> scenario = ActivityScenario.launch(mockLocationIntent);

        // Wait for the activity to fully load and gain focus
        Thread.sleep(3000);

        // Ensure activity is ready
        scenario.onActivity(activity -> {
            // Activity is now guaranteed to be resumed
        });

        Thread.sleep(500);

        // Assert: Verify the displayed information matches the mocked Champaign location
        onView(withId(R.id.cityNameTextView))
                .check(matches(withText(CHAMPAIGN_NAME)));

        // Assert latitude is correctly displayed for Champaign
        onView(withId(R.id.latitudeTextView))
                .check(matches(withText(containsString("40.116"))));

        // Assert longitude is correctly displayed for Champaign
        onView(withId(R.id.longitudeTextView))
                .check(matches(withText(containsString("-88.243"))));

        scenario.close();
    }
}
