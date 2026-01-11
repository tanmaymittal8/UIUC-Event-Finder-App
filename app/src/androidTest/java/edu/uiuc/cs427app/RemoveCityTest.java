package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
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
    Test for removing a city. Goes through the steps and
    adds assertions to verify that all the steps
    are working correctly for the process of removing a city.
 */
@RunWith(AndroidJUnit4.class)
public class RemoveCityTest {
    @Rule
    public ActivityScenarioRule<LoginActivity> removeCityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    /**
     * Sets up the test environment before each test.
     * Registers a test user if not already registered, logs in through the UI,
     * and adds a city (Chicago) that can be removed during the test.
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
        
        // Add a city first so we can remove it
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
    }

    /**
     * Tests the functionality of removing a city from the user's location list.
     * Steps:
     * 1. Clicks the "Remove" button next to the CHICAGO city entry
     * 2. Waits for the removal to complete
     * 3. Verifies that "CHICAGO" no longer exists in the city list
     * @throws InterruptedException if thread sleep is interrupted
     */
    @Test
    public void removeAddCity() throws InterruptedException{
        // Click the Remove button that is a sibling of CHICAGO text
        onView(allOf(withText("Remove"), hasSibling(withText(containsString("CHICAGO")))))
                .perform(click());
        Thread.sleep(1500);
        onView(withText(containsString("CHICAGO")))
                .check(doesNotExist());
    }
}
