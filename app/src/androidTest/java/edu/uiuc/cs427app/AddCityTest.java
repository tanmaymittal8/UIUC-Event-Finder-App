package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/*
    Test for adding a city. Goes through the steps and
    adds assertions to verify that all the steps
    are working correctly for the process of adding a city.
    Note: in this case, we select the first city from the
    list.
 */

// referred to https://stackoverflow.com/questions/37412899/espresso-check-all-in-list
// referred to https://stackoverflow.com/questions/29250506/espresso-how-to-check-if-one-of-the-view-is-displayed

@RunWith(AndroidJUnit4.class)
public class AddCityTest {
    @Rule
    public ActivityScenarioRule<LoginActivity> addCityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    /**
     * Sets up the test environment before each test.
     * Registers a test user if not already registered and logs in through the UI.
     * Waits for MainActivity to load before proceeding with tests.
     * @throws InterruptedException if thread sleep is interrupted
     */
    @Before
    public void setup() throws InterruptedException {
        // Create test user if doesn't exist
        Context context = ApplicationProvider.getApplicationContext();
        AuthenticationManager authManager = AuthenticationManager.getInstance(context);
        
        // Try to register the test user (will fail silently if already exists)
        authManager.register("testuser", "password123", Theme.LIGHT);
        
        // Login
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
     * Tests the functionality of adding a city to the user's location list.
     * Steps:
     * 1. Clicks the "Add Location" button
     * 2. Types "Chicago" in the search input
     * 3. Clicks "Add" to search for the city
     * 4. Selects the first city from the search results
     * 5. Verifies that "CHICAGO" is displayed in the city list
     * @throws InterruptedException if thread sleep is interrupted
     */
    @Test
    public void testAddCity() throws InterruptedException{
        onView(withId(R.id.buttonAddLocation))
                .perform(click());
        Thread.sleep(500);
        onView(withId(R.id.searchCityInput))
                .perform(typeText("Chicago"), closeSoftKeyboard());
        Thread.sleep(500);
        onView(withText("Add")).perform(click());
        Thread.sleep(2000);
        onData(anything()).atPosition(0)
                .perform(click());
        Thread.sleep(1500);
        onView(withText(containsString("CHICAGO")))
                .check(matches(isDisplayed()));
    }
}
