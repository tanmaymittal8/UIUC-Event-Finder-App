package edu.uiuc.cs427app;

import android.content.Context;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.widget.EditText;

import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static org.hamcrest.Matchers.allOf;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.*;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 Instrumented test class responsible for validating city location feature is working properly.
 Ensures that once a user adds a city and goes through the steps of clicking on show weather and
 then show map the user is able to see a google maps view of the selected city along with the latitude and longitude data
 The test provides assertions that confirm expected UI behavior.
 */
@RunWith(AndroidJUnit4.class)
public class LocationFeatureTest {

    /**
     * Ensures user is logged out and cities are cleared before tests
     */
    @org.junit.BeforeClass
    public static void setUpClass() {
        Context context = ApplicationProvider.getApplicationContext();
        AuthenticationManager authManager = AuthenticationManager.getInstance(context);
        authManager.logout(); // Ensure logged out before any test
    }
    
    /**
     * Ensures user is logged out after each test
     */
    @org.junit.After
    public void tearDown() {
        Context context = ApplicationProvider.getApplicationContext();
        AuthenticationManager authManager = AuthenticationManager.getInstance(context);
        authManager.logout(); // Logout after each test
    }

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    /**
     * Ensures there is a logged in user before each test run
     * Logs in through UI before each test
     */
    @Before
    public void setUp() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        AuthenticationManager authManager = AuthenticationManager.getInstance(context);
        
        // Register test user (will fail silently if already exists)
        authManager.register("testuser", "password123", Theme.LIGHT);
        
        // Login through UI using typeText like AddCityTest
        onView(withId(R.id.usernameEditText))
                .perform(typeText("testuser"), closeSoftKeyboard());
        Thread.sleep(500);
        onView(withId(R.id.passwordEditText))
                .perform(typeText("password123"), closeSoftKeyboard());
        Thread.sleep(500);
        onView(withId(R.id.loginButton))
                .perform(click());
        Thread.sleep(3000); // Wait for MainActivity to load
    }

    /**
     * Helper function to add a city before we can test if desired location is visible
     * This test contains assertions to make sure the the correct city is added to ensure the prerequisite setup is completed before we can see the location info
     */
    public void helper_add_city(String City_name) throws InterruptedException {
        onView(withId(R.id.buttonAddLocation)).perform(click());
        Thread.sleep(500);

        onView(withId(R.id.searchCityInput)).perform(typeText(City_name), closeSoftKeyboard());
        Thread.sleep(500);

        onView(withText("Add")).perform(click());
        Thread.sleep(2000);

        onData(anything()).atPosition(0).perform(click());
        Thread.sleep(1500);

        onView(withText(containsString(City_name.toUpperCase()))).check(matches(isDisplayed()));
    }

    /**
     * Tests that when the user wants to see the location of Boston the user is able to see the google map along with the longitude and latitude of the city
     * 1. Adds Boston city on the app
     * 2. Clicks on Show weather button corresponding to the correct city, Boston in this case
     * 3. Clicks on Show Map button to see the google map of the city along with latitude and longitude data
     * 2. Asserts that the google map show the city is avaliable along with the latitude and longitude data
     * @throws InterruptedException
     */
    @Test
    public void testValidUserCity_Boston() throws InterruptedException{

        helper_add_city("Boston");
        Thread.sleep(1000);
        onView(allOf(withText("SHOW WEATHER"), hasSibling(withText(containsString("BOSTON"))))).perform(click());
        Thread.sleep(3000);
        onView(withText("SHOW MAP")).perform(click());
        Thread.sleep(3000);

        onView(withId(R.id.mapWebView)).check(matches(isDisplayed()));
        Thread.sleep(500);

        onView(withId(R.id.cityNameTextView)).check(matches(withText(containsString("Boston"))));
        Thread.sleep(500);

        onView(withId(R.id.latitudeTextView)).check(matches(withText(containsString("42"))));
        Thread.sleep(500);
        onView(withId(R.id.longitudeTextView)).check(matches(withText(containsString("-71"))));



    }

    

    /**
     * Tests that when the user wants to see the location of Dallas the user is able to see the google map along with the longitude and latitude of the city
     * 1. Adds Dallas city on the app
     * 2. Clicks on Show weather button corresponding to the correct city, Dallas in this case
     * 3. Clicks on Show Map button to see the google map of the city along with latitude and longitude data
     * 2. Asserts that the google map show the city is avaliable along with the latitude and longitude data
     * @throws InterruptedException
     */
    @Test
    public void testValidUserCity_Dallas() throws InterruptedException {

        helper_add_city("Dallas");

        Thread.sleep(1000);
        onView(allOf(withText("SHOW WEATHER"), hasSibling(withText(containsString("DALLAS"))))).perform(click());
        Thread.sleep(3000);
        onView(withText("SHOW MAP")).perform(click());
        Thread.sleep(3000);

        onView(withId(R.id.mapWebView)).check(matches(isDisplayed()));
        Thread.sleep(500);

        onView(withId(R.id.cityNameTextView)).check(matches(withText(containsString("Dallas"))));
        Thread.sleep(500);

        onView(withId(R.id.latitudeTextView)).check(matches(withText(containsString("32"))));
        Thread.sleep(500);
        onView(withId(R.id.longitudeTextView)).check(matches(withText(containsString("-96"))));

    }

}
