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
 * LLM-generated logic tests for the user sign-up functionality.
 *
 * Focus: AuthenticationManager.register(username, password, theme)
 */
@RunWith(AndroidJUnit4.class)
public class AuthenticationManagerUserSignUpTest {

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
    }

    @Test
    public void testUserSignUp_validNewUser_persistsUserInDatabase() {
        String username = "signup_valid_" + System.currentTimeMillis();
        String password = "password123";

        boolean result = authManager.register(username, password, Theme.LIGHT);

        assertTrue("Registration should succeed for a new user", result);

        // Now query the SAME DatabaseHelper instance used during registration
        User stored = databaseHelper.getUserByUsername(username);
        assertNotNull("User should be present in the database after registration", stored);
        assertEquals("Stored username should match the input username", username, stored.getUsername());
        assertNotEquals("Password should be stored as a hash, not plain text",
                password, stored.getPasswordHash());
    }

    @Test
    public void testUserSignUp_duplicateUsername_registrationFails() {
        String username = "signup_duplicate_" + System.currentTimeMillis();
        String password = "password123";

        boolean firstAttempt = authManager.register(username, password, Theme.LIGHT);
        boolean secondAttempt = authManager.register(username, "anotherPassword", Theme.DARK);

        assertTrue("First registration for a unique username should succeed", firstAttempt);
        assertFalse("Second registration with the same username should fail", secondAttempt);
    }

    @Test
    public void testUserSignUp_invalidInput_emptyOrNull_rejected() {
        boolean resultEmptyUsername = authManager.register("", "somePass", Theme.LIGHT);
        boolean resultEmptyPassword =
                authManager.register("user_empty_pass_" + System.currentTimeMillis(), "", Theme.LIGHT);
        boolean resultNullUsername = authManager.register(null, "somePass", Theme.LIGHT);
        boolean resultNullPassword =
                authManager.register("user_null_pass_" + System.currentTimeMillis(), null, Theme.LIGHT);

        assertFalse("Empty username should be rejected", resultEmptyUsername);
        assertFalse("Empty password should be rejected", resultEmptyPassword);
        assertFalse("Null username should be rejected", resultNullUsername);
        assertFalse("Null password should be rejected", resultNullPassword);
    }
}
