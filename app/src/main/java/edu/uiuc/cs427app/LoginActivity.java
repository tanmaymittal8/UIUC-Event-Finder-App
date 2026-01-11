package edu.uiuc.cs427app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * LoginActivity handles user authentication.
 * Provides sign in and sign up functionality.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText usernameField;
    private EditText passwordField;
    private EditText themePreference;
    private Button loginButton;
    private Button registerButton;
    private Button resetButton;
    private AuthenticationManager authManager;
//    private RadioGroup themeGroup;
//    private RadioButton themeLight, themeDark, themeSystem;

    /**
     * Called when the activity is first created.
     * Initializes UI components and sets up event listeners.
     *
     * @param savedInstanceState Saved state from previous instance
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Theme t = Theme.LIGHT;
        applyTheme(t);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = AuthenticationManager.getInstance(this);

        // Check if user is already logged in
        if (authManager.isLoggedIn()) {
            String username = authManager.getCurrentUser() != null ? authManager.getCurrentUser().getUsername() : null;
            navigateToMainActivity(username);
            return;
        }

        // Initialize UI components
        usernameField = findViewById(R.id.usernameEditText);
        passwordField = findViewById(R.id.passwordEditText);
        themePreference = findViewById(R.id.themePromptEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        resetButton = findViewById(R.id.resetPasswordButton);
        loginButton.setOnClickListener(v -> onLoginButtonClick());
        registerButton.setOnClickListener(v -> onRegisterButtonClick());
        resetButton.setOnClickListener(v -> onResetPasswordButtonClick());
    }

    /**
     * Applies the specified theme to the activity.
     *
     * @param t The theme to apply (LIGHT or DARK)
     */
    private void applyTheme(Theme t) {
        int mode = (t == Theme.DARK)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);
    }

//    private Theme getSelectedTheme() {
//        int id = themeGroup.getCheckedRadioButtonId();
////        if (id == R.id.themeLight)  return Theme.LIGHT;
////        if (id == R.id.themeDark)   return Theme.DARK;
//        return Theme.SYSTEM;
//    }

    /**
     * Handles the login button click event.
     * Validates input and attempts to log in the user.
     */
    private void onLoginButtonClick() {
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Attempt login
        boolean success = authManager.login(username, password);

        if (success) {
            String themeDescription = themePreference != null ? themePreference.getText().toString().trim() : "";

            if (themeDescription.isEmpty()) {
                // No new prompt: reuse saved theme if available
                ThemeSpec saved = AuthenticationManager.getInstance(this)
                        .loadThemeSpecForUser(username);
                if (saved != null) {
                    navigateToMainActivity(username);
                } else {
                    Toast.makeText(this, "Login successful, generating theme...", Toast.LENGTH_SHORT).show();
                    LLMClient.generateThemeSpecAsync("", spec -> {
                        ThemeManager.saveForUser(this, username, spec);
                        navigateToMainActivity(username);
                    });
                }
            } else {
                Toast.makeText(this, "Login successful, generating theme...", Toast.LENGTH_SHORT).show();
                LLMClient.generateThemeSpecAsync(themeDescription, spec -> {
                    ThemeManager.saveForUser(this, username, spec);
                    navigateToMainActivity(username);
                });
            }
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the register button click event.
     * Validates input and attempts to register a new user.
     */
    private void onRegisterButtonClick() {
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean success = authManager.register(username, password, Theme.Default);

        if (success) {
            String themeDescription = themePreference != null ? themePreference.getText().toString().trim() : "";

            Toast.makeText(this, "Registration successful! Generating theme...", Toast.LENGTH_SHORT).show();

            LLMClient.generateThemeSpecAsync(themeDescription, spec -> {
                ThemeManager.saveForUser(this, username, spec);
                Toast.makeText(this, "Theme generated! Please login.", Toast.LENGTH_SHORT).show();
                passwordField.setText("");
            });
        } else {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the reset password button click event.
     * Validates input and attempts to reset user password.
     */
    private void onResetPasswordButtonClick() {
        String username = usernameField.getText().toString().trim();
        String newPassword = passwordField.getText().toString().trim();

        if (username.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Please enter username and new password", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = authManager.resetPassword(username, newPassword);

        if (success) {
            Toast.makeText(this, "Password reset. Please sign in.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Reset failed (user not found)", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Navigates to MainActivity after successful login.
     */
    private void navigateToMainActivity(String username) {
        Intent intent = new Intent(this, MainActivity.class);
        if (username != null && !username.isEmpty()) {
            intent.putExtra("username", username);
        }
        startActivity(intent);
        finish(); // Prevent going back to login screen with back button
    }
}
