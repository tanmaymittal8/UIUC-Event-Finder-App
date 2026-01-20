package edu.uiuc.cs427app;

/**
 * User model class to represent a user in the application.
 * Stores user ID, username, and hashed password for authentication.
 */
public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private Theme theme;

    private String themeJson;  // nullable

    private String name; // Add this
    private String bio;  // Add this


    /**
     * Constructor to create a new User object with user ID.
     *
     * @param userId       The unique user ID from database
     * @param username     The username of the user
     * @param passwordHash The hashed password for security
     */
    public User(String userId, String username, String passwordHash, Theme theme) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.theme = theme;
    }

    /**
     * Constructor to create a new User object without user ID.
     * Used for registration before database insertion.
     *
     * @param username     The username of the user
     * @param passwordHash The hashed password for security
     */
    public User(String username, String passwordHash) {
        this.userId = null;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    /**
     * Gets the user's theme preference.
     *
     * @return The theme (LIGHT, DARK, or SYSTEM)
     */
    public Theme getTheme() {
        return theme;
    }

    /**
     * Sets the user's theme preference.
     *
     * @param theme The theme to set
     */
    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    /**
     * Gets the user ID.
     *
     * @return The user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId The user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the username.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username The username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password hash.
     *
     * @return The hashed password
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the password hash.
     *
     * @param passwordHash The hashed password to set
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    /**
     * Gets the theme JSON specification.
     *
     * @return The theme JSON string or null
     */
    public String getThemeJson() {
        return themeJson;
    }

    /**
     * Sets the theme JSON specification.
     *
     * @param themeJson The theme JSON string to set
     */
    public void setThemeJson(String themeJson) {
        this.themeJson = themeJson;
    }
}
