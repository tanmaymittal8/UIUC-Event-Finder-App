package edu.uiuc.cs427app;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
    Instrumented test class responsible for validating the application's user log-off functionality.
    Ensures that once a user initiates the log off action, the app correctly returns to the log in screen, reflecting a fully-logged out state.
    The test provides assertions that confirm expected UI behavior.
 */
@RunWith(AndroidJUnit4.class)
public class UserLogOffTest {

    /**
     * Ensures user is logged out before activity launches
     */
    @org.junit.BeforeClass
    public static void setUpClass() {
        Context context = ApplicationProvider.getApplicationContext();
        AuthenticationManager authManager = AuthenticationManager.getInstance(context);
        authManager.logout(); // Ensure logged out before any test
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
     * Tests that tapping log out button from logged in Main Activity session returns to the login screen
     * 1. Logs in through UI
     * 2. Taps the log out button from Main Activity session
     * 3. Asserts that the login button on Login Activity is displayed again, confirming user has been logged out and returned to login screen
     * @throws InterruptedException
     */
    @Test
    public void testUserLogOffNavigatesToLoginScreen() throws InterruptedException {
        // Tap the log out button in Main Activity
        onView(withId(R.id.logoutButton)).perform(click());

        // Pause for demo vid
        Thread.sleep(2000);

        // Assert we're back on Login Activity by checking that login button is displayed
        onView(withId(R.id.loginButton)).check((matches(isDisplayed())));
    }
}
