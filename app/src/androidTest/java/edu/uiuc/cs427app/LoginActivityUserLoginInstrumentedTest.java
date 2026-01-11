package edu.uiuc.cs427app;

import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
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
 * LLM-generated Espresso instrumented tests for the user login scenario.
 *
 * Focus:
 *  - Validation: empty credentials should not log in a user
 *  - Negative: invalid credentials should show error and not log in
 *  - Happy path: valid credentials log in an existing user
 *  - Session: verify user session is maintained after successful login
 *
 * NOTE: These tests intentionally do NOT exercise the sign-up flow.
 * NOTE: LLM theme generation is mocked by not providing theme prompts.
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityUserLoginInstrumentedTest {

    private DatabaseHelper databaseHelper;
    private AuthenticationManager authManager;

    @BeforeClass
    public static void setUpClass() {
        // Ensure no user is logged in before ANY test runs
        // This must happen before ActivityScenarioRule creates the activity
        AuthenticationManager.getInstance(ApplicationProvider.getApplicationContext()).logout();
    }

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void setUp() {
        databaseHelper = new DatabaseHelper(ApplicationProvider.getApplicationContext());
        authManager = AuthenticationManager.getInstance(ApplicationProvider.getApplicationContext());
    }

    @After
    public void tearDown() {
        // Ensure logout after each test to prevent interference
        authManager.logout();
    }

    @Test
    public void testUserLogin_emptyCredentials_showsValidationAndDoesNotLogin() {
        // Leave username and password empty
        onView(withId(R.id.usernameEditText))
                .perform(replaceText(""), closeSoftKeyboard());
        SystemClock.sleep(500);
        onView(withId(R.id.passwordEditText))
                .perform(replaceText(""), closeSoftKeyboard());
        SystemClock.sleep(500);

        // Click the "Sign In" button
        onView(withId(R.id.loginButton)).perform(click());

        // Small delay to allow any async operations
        SystemClock.sleep(200);

        // Assertion 1 (UI-level): we are still on the login screen
        onView(withId(R.id.usernameEditText)).check(matches(isDisplayed()));

        // Assertion 2 (session-level): no user should be logged in
        assertFalse("No user should be logged in with empty credentials",
                authManager.isLoggedIn());
        assertNull("Current user should be null", authManager.getCurrentUser());
    }

    @Test
    public void testUserLogin_invalidUsername_loginFails() {
        String nonExistentUsername = "nonexistent_user_" + System.currentTimeMillis();
        String password = "somePassword";

        // Enter non-existent username
        onView(withId(R.id.usernameEditText))
                .perform(replaceText(nonExistentUsername), closeSoftKeyboard());
        SystemClock.sleep(500);
        onView(withId(R.id.passwordEditText))
                .perform(replaceText(password), closeSoftKeyboard());
        SystemClock.sleep(500);

        // Click "Sign In"
        onView(withId(R.id.loginButton)).perform(click());

        // Small delay
        SystemClock.sleep(200);

        // Assertion: should still be on login screen
        onView(withId(R.id.usernameEditText)).check(matches(isDisplayed()));

        // Assertion: user should not be logged in
        assertFalse("User should not be logged in with invalid username",
                authManager.isLoggedIn());
        assertNull("Current user should be null", authManager.getCurrentUser());
    }

    @Test
    public void testUserLogin_invalidPassword_loginFails() {
        // First, register a user directly via AuthenticationManager
        String username = "ui_login_wrong_pass_" + System.currentTimeMillis();
        String correctPassword = "correctPassword123";

        authManager.register(username, correctPassword, Theme.LIGHT);
        authManager.logout();

        // Now try to login with wrong password via UI
        onView(withId(R.id.usernameEditText))
                .perform(replaceText(username), closeSoftKeyboard());
        SystemClock.sleep(500);
        onView(withId(R.id.passwordEditText))
                .perform(replaceText("wrongPassword"), closeSoftKeyboard());
        SystemClock.sleep(500);

        // Click "Sign In"
        onView(withId(R.id.loginButton)).perform(click());

        // Small delay
        SystemClock.sleep(200);

        // Assertion: should still be on login screen
        onView(withId(R.id.usernameEditText)).check(matches(isDisplayed()));

        // Assertion: user should not be logged in
        assertFalse("User should not be logged in with wrong password",
                authManager.isLoggedIn());
        assertNull("Current user should be null", authManager.getCurrentUser());
    }

    @Test
    public void testUserLogin_validCredentials_loginSucceeds() {
        // First, register a user directly via AuthenticationManager
        String username = "ui_login_valid_" + System.currentTimeMillis();
        String password = "validPassword123";

        boolean registered = authManager.register(username, password, Theme.DARK);
        assertTrue("User should be registered", registered);

        // Pre-save a theme to avoid LLM generation during login
        ThemeSpec mockTheme = new ThemeSpec();
        mockTheme.backgroundHex = "#FFFFFF";
        mockTheme.textHex = "#111111";
        mockTheme.accentHex = "#FF0000";
        mockTheme.buttonHex = "#00FF00";
        mockTheme.secondaryHex = "#F5F5F5";
        authManager.saveThemeSpecForUser(username, mockTheme);

        authManager.logout();

        // Now login with correct credentials via UI (without theme prompt to avoid LLM)
        onView(withId(R.id.usernameEditText))
                .perform(replaceText(username), closeSoftKeyboard());
        SystemClock.sleep(500);
        onView(withId(R.id.passwordEditText))
                .perform(replaceText(password), closeSoftKeyboard());
        SystemClock.sleep(500);
        // Leave theme prompt empty to use saved theme
        onView(withId(R.id.themePromptEditText))
                .perform(replaceText(""), closeSoftKeyboard());
        SystemClock.sleep(500);

        // Click "Sign In"
        onView(withId(R.id.loginButton)).perform(click());

        // Wait for login to complete (theme is already saved, so no LLM delay)
        SystemClock.sleep(500);

        // Assertion 1 (session-level): user should be logged in
        assertTrue("User should be logged in after successful login",
                authManager.isLoggedIn());

        // Assertion 2 (session-level): current user should match
        User currentUser = authManager.getCurrentUser();
        assertNotNull("Current user should not be null after login", currentUser);
        assertEquals("Current user username should match logged-in user",
                username, currentUser.getUsername());

        // Assertion 3 (data-level): user exists in database
        User dbUser = databaseHelper.getUserByUsername(username);
        assertNotNull("User should exist in database", dbUser);
        assertEquals("Database username should match", username, dbUser.getUsername());
    }

    @Test
    public void testUserLogin_sessionPersistence_afterSuccessfulLogin() {
        // Register a user and pre-save theme
        String username = "ui_session_test_" + System.currentTimeMillis();
        String password = "sessionPassword123";

        authManager.register(username, password, Theme.LIGHT);

        // Pre-save a theme to avoid LLM generation
        ThemeSpec mockTheme = new ThemeSpec();
        mockTheme.backgroundHex = "#000000";
        mockTheme.textHex = "#FFFFFF";
        mockTheme.accentHex = "#0000FF";
        mockTheme.buttonHex = "#FFFF00";
        mockTheme.secondaryHex = "#333333";
        authManager.saveThemeSpecForUser(username, mockTheme);

        authManager.logout();

        // Login via UI
        onView(withId(R.id.usernameEditText))
                .perform(replaceText(username), closeSoftKeyboard());
        SystemClock.sleep(500);
        onView(withId(R.id.passwordEditText))
                .perform(replaceText(password), closeSoftKeyboard());
        SystemClock.sleep(500);
        onView(withId(R.id.themePromptEditText))
                .perform(replaceText(""), closeSoftKeyboard());
        SystemClock.sleep(500);

        onView(withId(R.id.loginButton)).perform(click());

        SystemClock.sleep(500);

        // Verify session is maintained
        assertTrue("Session should be active (isLoggedIn)", authManager.isLoggedIn());

        User sessionUser = authManager.getCurrentUser();
        assertNotNull("Session user should not be null", sessionUser);
        assertEquals("Session username should match", username, sessionUser.getUsername());

        // Verify the session persists (would survive across activity recreations)
        // by creating a new AuthenticationManager instance
        AuthenticationManager newAuthManagerInstance =
                AuthenticationManager.getInstance(ApplicationProvider.getApplicationContext());
        assertTrue("Session should persist across instances",
                newAuthManagerInstance.isLoggedIn());
        assertEquals("Session user should match across instances",
                username, newAuthManagerInstance.getCurrentUser().getUsername());
    }

    @Test
    public void testUserLogin_multipleFailedAttempts_thenSuccess() {
        // Register a user with theme
        String username = "ui_multi_attempt_" + System.currentTimeMillis();
        String password = "correctPassword";

        authManager.register(username, password, Theme.DARK);

        // Pre-save theme
        ThemeSpec mockTheme = new ThemeSpec();
        mockTheme.backgroundHex = "#AAAAAA";
        mockTheme.textHex = "#111111";
        mockTheme.accentHex = "#123456";
        mockTheme.buttonHex = "#654321";
        mockTheme.secondaryHex = "#CCCCCC";
        authManager.saveThemeSpecForUser(username, mockTheme);

        authManager.logout();

        // First failed attempt
        onView(withId(R.id.usernameEditText))
                .perform(replaceText(username), closeSoftKeyboard());
        SystemClock.sleep(500);
        onView(withId(R.id.passwordEditText))
                .perform(replaceText("wrong1"), closeSoftKeyboard());
        SystemClock.sleep(500);
        onView(withId(R.id.loginButton)).perform(click());
        SystemClock.sleep(1000);

        assertFalse("First failed attempt: should not be logged in",
                authManager.isLoggedIn());

        // Second failed attempt
        onView(withId(R.id.passwordEditText))
                .perform(replaceText("wrong2"), closeSoftKeyboard());
        SystemClock.sleep(500);
        onView(withId(R.id.loginButton)).perform(click());
        SystemClock.sleep(1000);

        assertFalse("Second failed attempt: should not be logged in",
                authManager.isLoggedIn());

        // Successful attempt
        onView(withId(R.id.passwordEditText))
                .perform(replaceText(password), closeSoftKeyboard());
        SystemClock.sleep(500);
        onView(withId(R.id.themePromptEditText))
                .perform(replaceText(""), closeSoftKeyboard());
        SystemClock.sleep(500);
        onView(withId(R.id.loginButton)).perform(click());
        SystemClock.sleep(500);

        // Should now be logged in
        assertTrue("After correct password: should be logged in",
                authManager.isLoggedIn());
        assertEquals("Logged in username should match",
                username, authManager.getCurrentUser().getUsername());
    }
}
