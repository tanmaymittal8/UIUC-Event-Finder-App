package edu.uiuc.cs427app;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Singleton class to manage user authentication.
 * Handles login, registration, logout, and session management.
 * Uses SQLite database for user storage and SharedPreferences for session management.
 */
public class AuthenticationManager {
    private static final String PREFS_NAME = "CS427AppPrefs";
    private static final String KEY_CURRENT_USER = "current_user";
    private static AuthenticationManager instance;
    private final SharedPreferences sharedPreferences;
    private final DatabaseHelper databaseHelper;
    private final Gson gson;
    private User currentUser;

    /**
     * Private constructor for singleton pattern.
     *
     * @param context Application context
     */
    private AuthenticationManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(context);
        gson = new Gson();
        loadCurrentUser();
    }

    // Add this method to AuthenticationManager.java

    /**
     * Updates the user's profile information (Name and Bio).
     *
     * @param name The real name of the user
     * @param bio The user's biography
     * @return true if update successful
     */
    public boolean updateUserProfile(String name, String bio) {
        if (currentUser == null) return false;

        // 1. Update the in-memory user object
        currentUser.setName(name);
        currentUser.setBio(bio);

        // 2. Save to SharedPreferences so it persists across app restarts
        saveCurrentUser();

        // 3. Update the SQLite Database
        // You must add this method to your DatabaseHelper class!
        return databaseHelper.updateUserProfile(currentUser.getUsername(), name, bio);
    }

    /**
     * Gets the singleton instance of AuthenticationManager.
     *
     * @param context Application context
     * @return The AuthenticationManager instance
     */
    public static synchronized AuthenticationManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthenticationManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Registers a new user with username and password.
     *
     * @param username The username to register
     * @param password The password to register
     * @return true if registration successful, false if username already exists
     */
    public boolean register(String username, String password, Theme theme) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return false;
        }

        // Check if username already exists
        if (databaseHelper.userExists(username)) {
            return false;
        }

        // Create new user with hashed password
        String passwordHash = hashPassword(password);

        // Insert user into database
        long userId = databaseHelper.insertUser(username, passwordHash, theme);

        return userId != -1;
    }

    /**
     * Logs in a user with username and password.
     *
     * @param username The username to login
     * @param password The password to login
     * @return true if login successful, false otherwise
     */
    public boolean login(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        // Retrieve user from database
        User user = databaseHelper.getUserByUsername(username);

        if (user == null) {
            return false;
        }

        // Verify password
        String passwordHash = hashPassword(password);
        if (user.getPasswordHash().equals(passwordHash)) {
            currentUser = user;
            saveCurrentUser();
            return true;
        }

        return false;
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        currentUser = null;
        sharedPreferences.edit().remove(KEY_CURRENT_USER).apply();
    }

    /**
     * Gets the currently logged-in user.
     *
     * @return The current User object, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Resets password for a user (placeholder for future implementation).
     *
     * @param username The username to reset password for
     * @return true if reset successful, false otherwise
     */
    public boolean resetPassword(String username, String newPassword) {
        if (username == null || newPassword == null) return false;
        username = username.trim();

        // find user
        User user = databaseHelper.getUserByUsername(username);
        if (user == null) return false;

        // update to new password
        String newHash = hashPassword(newPassword);
        boolean updated = databaseHelper.updateUserPassword(username, newHash);
        if (!updated) return false;

        // keep in-memory session consistent
        if (currentUser != null && username.equals(currentUser.getUsername())) {
            currentUser.setPasswordHash(newHash);
            saveCurrentUser();
        }
        return true;
    }

    /**
     * Hashes a password using SHA-256.
     *
     * @param password The password to hash
     * @return The hashed password as a hexadecimal string
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback (not secure, but prevents crash)
        }
    }

    /**
     * Saves the current user session to SharedPreferences.
     */
    private void saveCurrentUser() {
        if (currentUser != null) {
            String userJson = gson.toJson(currentUser);
            sharedPreferences.edit().putString(KEY_CURRENT_USER, userJson).apply();
        }
    }

    /**
     * Loads the current user session from SharedPreferences.
     */
    private void loadCurrentUser() {
        String userJson = sharedPreferences.getString(KEY_CURRENT_USER, null);
        if (userJson != null) {
            currentUser = gson.fromJson(userJson, User.class);
        }
    }

    /**
     * Saves a theme specification for a user to the database.
     * Updates in-memory user if it's the current user.
     *
     * @param username Username to save theme for
     * @param spec     ThemeSpec to save
     */
    public void saveThemeSpecForUser(String username, ThemeSpec spec) {
        if (username == null || username.trim().isEmpty() || spec == null) return;
        String json = spec.toJson();
        databaseHelper.updateUserThemeJson(username, json);
        // keep the in-memory user in sync if it's the current one
        if (currentUser != null && username.equals(currentUser.getUsername())) {
            currentUser.setThemeJson(json);
            saveCurrentUser();
        }
    }

    /**
     * Loads a theme specification for a user from the database.
     *
     * @param username Username to load theme for
     * @return ThemeSpec if found, null otherwise
     */
    public ThemeSpec loadThemeSpecForUser(String username) {
        if (username == null || username.trim().isEmpty()) return null;
        String json = databaseHelper.getUserThemeJson(username);
        if (json == null || json.isEmpty()) return null;
        try {
            return ThemeSpec.fromJson(json);
        } catch (Exception e) {
            return null;
        }
    }
}
