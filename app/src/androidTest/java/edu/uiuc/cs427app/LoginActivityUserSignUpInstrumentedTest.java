package edu.uiuc.cs427app;

import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

/**
 * LLM-generated Espresso instrumented tests for the user sign-up scenario.
 *
 * Focus:
 *  - Validation: empty credentials should not sign up a user
 *  - Happy path: valid credentials create a new user in the database
 *
 * NOTE: These tests intentionally do NOT exercise the login flow.
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityUserSignUpInstrumentedTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void testUserSignUp_emptyUsernameAndPassword_showsValidationAndDoesNotCreateUser() {
        // Leave username and password empty
        onView(withId(R.id.usernameEditText))
                .perform(replaceText(""), closeSoftKeyboard());
        SystemClock.sleep(500);
        onView(withId(R.id.passwordEditText))
                .perform(replaceText(""), closeSoftKeyboard());
        SystemClock.sleep(500);

        // Click the "Sign Up" / register button
        onView(withId(R.id.registerButton)).perform(click());
        SystemClock.sleep(500);

        // Action: click sign-up with invalid inputs.
        // Assertion 1 (UI-level): we are still on the login screen (a login view is visible).
        onView(withId(R.id.usernameEditText)).check(matches(isDisplayed()));

        // Assertion 2 (data-level): no user with empty username was created.
        DatabaseHelper db = new DatabaseHelper(ApplicationProvider.getApplicationContext());
        User stored = db.getUserByUsername("");
        assertNull("No user should be created with an empty username", stored);
    }

    @Test
    public void testUserSignUp_validCredentials_createsUserInDatabase() {
        String username = "ui_signup_" + System.currentTimeMillis();
        String password = "password123";

        // Fill in username and password
        onView(withId(R.id.usernameEditText))
                .perform(replaceText(username), closeSoftKeyboard());
        SystemClock.sleep(500);
        onView(withId(R.id.passwordEditText))
                .perform(replaceText(password), closeSoftKeyboard());
        SystemClock.sleep(500);

        // Provide a theme description (the actual LLM content isn't asserted)
        onView(withId(R.id.themePromptEditText))
                .perform(replaceText("sunny beach theme"), closeSoftKeyboard());
        SystemClock.sleep(500);

        // Click "Sign Up"
        onView(withId(R.id.registerButton)).perform(click());

        // register(...) writes to DB synchronously; small delay is just to be safe
        SystemClock.sleep(300);

        // Assertion: user is now present in the database
        DatabaseHelper db = new DatabaseHelper(ApplicationProvider.getApplicationContext());
        User stored = db.getUserByUsername(username);
        assertNotNull("User should be created and stored in the database", stored);
        assertEquals("Stored username should match the sign-up input", username, stored.getUsername());
    }
}
