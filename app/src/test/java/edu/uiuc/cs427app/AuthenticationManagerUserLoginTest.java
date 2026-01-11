package edu.uiuc.cs427app;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * LLM-generated logic tests for the user login functionality.
 *
 * Focus: AuthenticationManager.login(username, password)
 */
@RunWith(AndroidJUnit4.class)
public class AuthenticationManagerUserLoginTest {

    private AuthenticationManager authManager;
    private DatabaseHelper databaseHelper;

    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        authManager = AuthenticationManager.getInstance(context);

        // *** IMPORTANT ***
        // Use the SAME DatabaseHelper instance that AuthenticationManager uses,
        // so we see the exact same DB state in our assertions.
        Field dbField = AuthenticationManager.class.getDeclaredField("databaseHelper");
        dbField.setAccessible(true);
        databaseHelper = (DatabaseHelper) dbField.get(authManager);

        // Ensure we start with a logged-out state
        authManager.logout();
    }

    @Test
    public void testUserLogin_validCredentials_loginSucceeds() {
        // First, register a user
        String username = "login_valid_" + System.currentTimeMillis();
        String password = "password123";

        boolean registered = authManager.register(username, password, Theme.LIGHT);
        assertTrue("User should be registered successfully", registered);

        // Logout to clear session
        authManager.logout();
        assertFalse("User should be logged out", authManager.isLoggedIn());

        // Now attempt login with correct credentials
        boolean loginSuccess = authManager.login(username, password);

        assertTrue("Login should succeed with valid credentials", loginSuccess);
        assertTrue("User should be logged in after successful login", authManager.isLoggedIn());
        assertNotNull("Current user should not be null after login", authManager.getCurrentUser());
        assertEquals("Current user username should match logged-in user",
                username, authManager.getCurrentUser().getUsername());
    }

    @Test
    public void testUserLogin_invalidPassword_loginFails() {
        // First, register a user
        String username = "login_wrong_pass_" + System.currentTimeMillis();
        String password = "correctPassword";

        boolean registered = authManager.register(username, password, Theme.LIGHT);
        assertTrue("User should be registered successfully", registered);

        // Logout to clear session
        authManager.logout();

        // Attempt login with incorrect password
        boolean loginSuccess = authManager.login(username, "wrongPassword");

        assertFalse("Login should fail with incorrect password", loginSuccess);
        assertFalse("User should not be logged in after failed login", authManager.isLoggedIn());
        assertNull("Current user should be null after failed login", authManager.getCurrentUser());
    }

    @Test
    public void testUserLogin_nonExistentUser_loginFails() {
        String username = "nonexistent_user_" + System.currentTimeMillis();
        String password = "somePassword";

        // Attempt login without registering
        boolean loginSuccess = authManager.login(username, password);

        assertFalse("Login should fail for non-existent user", loginSuccess);
        assertFalse("User should not be logged in", authManager.isLoggedIn());
        assertNull("Current user should be null", authManager.getCurrentUser());
    }

    @Test
    public void testUserLogin_emptyCredentials_loginFails() {
        // Test empty username
        boolean resultEmptyUsername = authManager.login("", "somePassword");
        assertFalse("Login should fail with empty username", resultEmptyUsername);
        assertFalse("User should not be logged in", authManager.isLoggedIn());

        // Test empty password
        boolean resultEmptyPassword = authManager.login("someUser", "");
        assertFalse("Login should fail with empty password", resultEmptyPassword);
        assertFalse("User should not be logged in", authManager.isLoggedIn());

        // Test both empty
        boolean resultBothEmpty = authManager.login("", "");
        assertFalse("Login should fail with both empty", resultBothEmpty);
        assertFalse("User should not be logged in", authManager.isLoggedIn());
    }

    @Test
    public void testUserLogin_nullCredentials_loginFails() {
        // Test null username
        boolean resultNullUsername = authManager.login(null, "somePassword");
        assertFalse("Login should fail with null username", resultNullUsername);
        assertFalse("User should not be logged in", authManager.isLoggedIn());

        // Test null password
        boolean resultNullPassword = authManager.login("someUser", null);
        assertFalse("Login should fail with null password", resultNullPassword);
        assertFalse("User should not be logged in", authManager.isLoggedIn());

        // Test both null
        boolean resultBothNull = authManager.login(null, null);
        assertFalse("Login should fail with both null", resultBothNull);
        assertFalse("User should not be logged in", authManager.isLoggedIn());
    }

    @Test
    public void testUserLogin_sessionPersistence_maintainsState() {
        // Register and login a user
        String username = "session_test_" + System.currentTimeMillis();
        String password = "password123";

        authManager.register(username, password, Theme.DARK);
        authManager.logout();

        boolean loginSuccess = authManager.login(username, password);
        assertTrue("Login should succeed", loginSuccess);

        // Verify session state is maintained
        assertTrue("isLoggedIn should return true", authManager.isLoggedIn());

        User currentUser = authManager.getCurrentUser();
        assertNotNull("getCurrentUser should return user object", currentUser);
        assertEquals("Username should match", username, currentUser.getUsername());

        // Verify user data persists in database
        User dbUser = databaseHelper.getUserByUsername(username);
        assertNotNull("User should exist in database", dbUser);
        assertEquals("Database username should match", username, dbUser.getUsername());
    }

    @Test
    public void testUserLogin_multipleFails_thenSuccess() {
        // Register a user
        String username = "multi_attempt_" + System.currentTimeMillis();
        String password = "correctPassword";

        authManager.register(username, password, Theme.LIGHT);
        authManager.logout();

        // Attempt login with wrong password multiple times
        assertFalse("First failed attempt", authManager.login(username, "wrong1"));
        assertFalse("Second failed attempt", authManager.login(username, "wrong2"));
        assertFalse("Third failed attempt", authManager.login(username, "wrong3"));

        // All should have failed
        assertFalse("User should not be logged in after failed attempts", authManager.isLoggedIn());

        // Now login with correct password
        boolean loginSuccess = authManager.login(username, password);
        assertTrue("Login should succeed with correct password", loginSuccess);
        assertTrue("User should be logged in", authManager.isLoggedIn());
    }
}
