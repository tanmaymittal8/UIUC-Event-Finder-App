package edu.uiuc.cs427app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity for displaying LLM-generated weather-related questions and answers.
 * Shows context-specific questions based on current weather data, and allows
 * users to click questions to receive personalized weather insights.
 */
public class WeatherInsightsActivity extends AppCompatActivity {
    private LinearLayout questionsLayout;
    private ProgressBar loadingProgressBar;
    private TextView loadingTextView;
    private ScrollView scrollView;
    private String weatherData;
    private String cityName;

    /**
     * Initializes the activity, loads theme, and sets up UI components.
     * Retrieves weather data and city name from intent, then generates questions.
     *
     * @param savedInstanceState Saved state from previous instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_insights);

        // Apply theme
        String username = AuthenticationManager.getInstance(this).getCurrentUser() != null
                ? AuthenticationManager.getInstance(this).getCurrentUser().getUsername()
                : "";
        ThemeSpec spec = ThemeManager.loadForUser(this, username);
        ThemeManager.apply(this, spec);

        // Get data from intent
        weatherData = getIntent().getStringExtra("weatherData");
        cityName = getIntent().getStringExtra("cityName");

        if (weatherData == null || weatherData.isEmpty()) {
            Toast.makeText(this, "Weather data not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        questionsLayout = findViewById(R.id.questionsLayout);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        loadingTextView = findViewById(R.id.loadingTextView);
        scrollView = findViewById(R.id.scrollView);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        TextView titleTextView = findViewById(R.id.titleTextView);
        if (cityName != null && !cityName.isEmpty()) {
            titleTextView.setText("Weather Insights for " + cityName);
        } else {
            titleTextView.setText("Weather Insights");
        }

        // Generate questions asynchronously
        generateQuestions();
    }

    /**
     * Generates weather-related questions using LLM asynchronously.
     * Shows loading indicator while generating, then displays questions as buttons.
     */
    private void generateQuestions() {
        // Show loading state
        loadingProgressBar.setVisibility(View.VISIBLE);
        loadingTextView.setVisibility(View.VISIBLE);
        loadingTextView.setText("Generating personalized questions...");
        questionsLayout.setVisibility(View.GONE);

        // Generate questions asynchronously
        LLMClient.generateWeatherQuestionsAsync(weatherData, new LLMClient.WeatherQuestionsCallback() {
            @Override
            public void onQuestionsGenerated(String[] questions) {
                runOnUiThread(() -> {
                    // Hide loading state
                    loadingProgressBar.setVisibility(View.GONE);
                    loadingTextView.setVisibility(View.GONE);
                    questionsLayout.setVisibility(View.VISIBLE);

                    // Display questions as buttons
                    displayQuestions(questions);
                });
            }
        });
    }

    /**
     * Displays the generated questions as clickable buttons.
     *
     * @param questions Array of question strings to display
     */
    private void displayQuestions(String[] questions) {
        questionsLayout.removeAllViews();

        TextView instructionsTextView = new TextView(this);
        instructionsTextView.setText("Tap a question to get personalized insights:");
        instructionsTextView.setTextSize(16);
        instructionsTextView.setPadding(0, 0, 0, 16);
        questionsLayout.addView(instructionsTextView);

        for (String question : questions) {
            Button questionButton = new Button(this);
            questionButton.setText(question);
            questionButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            questionButton.setPadding(16, 16, 16, 16);
            questionButton.setTextSize(14);
            questionButton.setOnClickListener(v -> showAnswer(question));
            questionsLayout.addView(questionButton);

            // Add spacing between buttons
            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    16));
            questionsLayout.addView(spacer);
        }
    }

    /**
     * Shows the answer to a selected question in a dialog.
     * Generates the answer using LLM asynchronously.
     *
     * @param question The question to answer
     */
    private void showAnswer(String question) {
        // Create and show loading dialog
        AlertDialog.Builder loadingDialogBuilder = new AlertDialog.Builder(this);
        loadingDialogBuilder.setTitle("Generating Answer");
        loadingDialogBuilder.setMessage("Please wait...");
        loadingDialogBuilder.setCancelable(false);
        AlertDialog loadingDialog = loadingDialogBuilder.create();
        loadingDialog.show();

        // Generate answer asynchronously
        LLMClient.generateWeatherAnswerAsync(weatherData, question, new LLMClient.WeatherAnswerCallback() {
            @Override
            public void onAnswerGenerated(String answer) {
                runOnUiThread(() -> {
                    loadingDialog.dismiss();

                    // Show answer in dialog
                    AlertDialog.Builder answerDialogBuilder = new AlertDialog.Builder(WeatherInsightsActivity.this);
                    answerDialogBuilder.setTitle(question);
                    answerDialogBuilder.setMessage(answer);
                    answerDialogBuilder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                    answerDialogBuilder.show();
                });
            }
        });
    }
}

