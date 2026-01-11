package edu.uiuc.cs427app;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

/**
 * Client for generating UI themes using the Gemini API.
 * Uses the official Gemini SDK for Android.
 */
public class LLMClient {
    private static final String TAG = "LLMClient";
    private static final String MODEL_NAME = "gemini-2.5-flash-lite";
    private static final int TIMEOUT_SECONDS = 20;

    private static final String API_KEY_PRIMARY = BuildConfig.GEMINI_API_KEY_PRIMARY;
    private static final String API_KEY_BACKUP = BuildConfig.GEMINI_API_KEY_BACKUP;

    private static boolean primaryKeyFailed = false;

    /**
     * Generate a theme JSON from a description (blocking call).
     */
    public static String generateThemeJson(String description) {
        if (description == null || description.trim().isEmpty()) {
            Log.d(TAG, "Empty description, using default theme");
            return getDefaultThemeJson();
        }

        try {
            String prompt = (description == null ? "" : description.trim()) + augmentThemeInstruction();

            if (!primaryKeyFailed && !API_KEY_PRIMARY.isEmpty()) {
                String result = generateWithKey(API_KEY_PRIMARY, prompt, "PRIMARY");
                if (result != null) return result;

                primaryKeyFailed = true;
                Log.w(TAG, "Primary key failed, trying backup");
            }

            if (!API_KEY_BACKUP.isEmpty()) {
                String result = generateWithKey(API_KEY_BACKUP, prompt, "BACKUP");
                if (result != null) return result;
            }

            Log.e(TAG, "Both API keys failed, using default");
            return getDefaultThemeJson();

        } catch (Exception e) {
            Log.e(TAG, "Error generating theme: " + e.getMessage(), e);
            return getDefaultThemeJson();
        }
    }

    /**
     * Call the Gemini API with a specific key.
     */
    private static String generateWithKey(String apiKey, String prompt, String keyLabel) {
        try {
            Log.d(TAG, "Generating theme with " + keyLabel + " key");

            GenerativeModel model = new GenerativeModel(MODEL_NAME, apiKey);
            GenerativeModelFutures futures = GenerativeModelFutures.from(model);

            Content content = new Content.Builder()
                    .addText(prompt)
                    .build();

            ListenableFuture<GenerateContentResponse> response = futures.generateContent(content);
            GenerateContentResponse result = response.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            String text = result.getText();
            if (text != null && !text.isEmpty()) {
                Log.d(TAG, keyLabel + " key succeeded");
                String json = extractJson(text);
                new JSONObject(json); // Validate
                return json;
            }

            Log.e(TAG, keyLabel + " key returned empty response");
            return null;

        } catch (java.util.concurrent.TimeoutException e) {
            Log.e(TAG, keyLabel + " timeout after " + TIMEOUT_SECONDS + "s");
            return null;
        } catch (Exception e) {
            Log.e(TAG, keyLabel + " error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generate a ThemeSpec from a description (blocking call).
     */
    public static ThemeSpec generateThemeSpec(String description) {
        String json = generateThemeJson(description);
        ThemeSpec spec = ThemeSpec.fromJson(json);

        if (spec == null || !spec.isValid()) {
            return ThemeSpec.defaultLight();
        }

        return spec;
    }

    /**
     * Generate a theme asynchronously with callback.
     */
    public static void generateThemeSpecAsync(String description, ThemeCallback callback) {
        new Thread(() -> {
            try {
                ThemeSpec spec = generateThemeSpec(description);
                postCallback(() -> callback.onThemeGenerated(spec));
            } catch (Exception e) {
                Log.e(TAG, "Async generation error: " + e.getMessage(), e);
                postCallback(() -> callback.onThemeGenerated(ThemeSpec.defaultLight()));
            }
        }).start();
    }

    /**
     * Same as generateThemeSpecAsync (for backward compatibility).
     */
    public static void generateThemeSpecAsyncWithSDK(String description, ThemeCallback callback) {
        generateThemeSpecAsync(description, callback);
    }

    /**
     * Post callback on main thread if available, otherwise call directly.
     */
    private static void postCallback(Runnable callback) {
        Looper mainLooper = Looper.getMainLooper();
        if (mainLooper != null) {
            new Handler(mainLooper).post(callback);
        } else {
            callback.run();
        }
    }

    /**
     * Build the prompt that tells Gemini how to generate themes.
     */
    private static String buildPrompt(String description) {
        return "You are a UI/UX designer. Create a color theme for: \"" + description + "\"\n\n" +
                "Return ONLY valid JSON (no markdown, no extra text):\n" +
                "{\n" +
                "  \"background\": \"#HEXCODE\",\n" +
                "  \"text\": \"#HEXCODE\",\n" +
                "  \"accent\": \"#HEXCODE\",\n" +
                "  \"button\": \"#HEXCODE\",\n" +
                "  \"secondary\": \"#HEXCODE\"\n" +
                "}\n\n" +
                "Requirements:\n" +
                "- Use 6-digit hex colors (e.g., #FF5733)\n" +
                "- Ensure good contrast between background and text\n" +
                "- Make colors match the mood and aesthetic of \"" + description + "\"\n" +
                "- Return ONLY the JSON, nothing else";
    }

    /**
     * Provides additional instructions for theme generation.
     *
     * @return Instruction string for LLM
     */
    private static String augmentThemeInstruction() {
        return "\n\nReturn ONLY a JSON object with these fields: " +
                "background, text, accent, button, secondary, cardBackground, borderColor, headerColor, emoji. " +
                "Emoji is optional. All colors must be hex like #RRGGBB. " +
                "Ensure readable contrast between text and background (aim ≥ 4.5:1).";
    }


    /**
     * Extract JSON from text that might have markdown or extra stuff.
     */
    private static String extractJson(String text) {
        text = text.trim();

        // Remove markdown code blocks
        if (text.contains("```json")) {
            int start = text.indexOf("```json") + 7;
            int end = text.lastIndexOf("```");
            if (end > start) {
                text = text.substring(start, end).trim();
            }
        } else if (text.contains("```")) {
            int start = text.indexOf("```") + 3;
            int end = text.lastIndexOf("```");
            if (end > start) {
                text = text.substring(start, end).trim();
            }
        }

        // Extract just the JSON object
        int jsonStart = text.indexOf('{');
        int jsonEnd = text.lastIndexOf('}');

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return text.substring(jsonStart, jsonEnd + 1);
        }

        return text;
    }

    /**
     * Default theme as fallback.
     */
    private static String getDefaultThemeJson() {
        return "{"
                + "\"background\":\"#FFFFFF\","
                + "\"text\":\"#111111\","
                + "\"accent\":\"#3D7DFF\","
                + "\"button\":\"#1976D2\","
                + "\"secondary\":\"#F5F5F5\","
                + "\"cardBackground\":\"#FFFFFF\","
                + "\"borderColor\":\"#DDDDDD\","
                + "\"headerColor\":\"#3D7DFF\","
                + "}";
    }

    /**
     * Generate weather-related questions based on current weather data.
     * Uses LLM to dynamically generate context-specific questions.
     *
     * @param weatherData String containing weather information (temperature, condition, humidity, wind, etc.)
     * @return Array of weather-related questions, or null if generation fails
     */
    public static String[] generateWeatherQuestions(String weatherData) {
        if (weatherData == null || weatherData.trim().isEmpty()) {
            Log.e(TAG, "Empty weather data provided");
            return new String[]{"What should I wear today?", "What should I prepare for an outdoor event today?"};
        }

        try {
            String prompt = buildWeatherQuestionsPrompt(weatherData);

            String result = null;
            if (!primaryKeyFailed && !API_KEY_PRIMARY.isEmpty()) {
                result = generateTextWithKey(API_KEY_PRIMARY, prompt, "PRIMARY");
                if (result != null) {
                    return parseQuestions(result);
                }
                primaryKeyFailed = true;
                Log.w(TAG, "Primary key failed for weather questions, trying backup");
            }

            if (!API_KEY_BACKUP.isEmpty()) {
                result = generateTextWithKey(API_KEY_BACKUP, prompt, "BACKUP");
                if (result != null) {
                    return parseQuestions(result);
                }
            }

            Log.e(TAG, "Both API keys failed for weather questions, using defaults");
            return new String[]{"What should I wear today?", "What should I prepare for an outdoor event today?"};

        } catch (Exception e) {
            Log.e(TAG, "Error generating weather questions: " + e.getMessage(), e);
            return new String[]{"What should I wear today?", "What should I prepare for an outdoor event today?"};
        }
    }

    /**
     * Generate an answer to a weather-related question based on current weather data.
     * Uses LLM to dynamically generate context-specific answers.
     *
     * @param weatherData String containing weather information
     * @param question    The question to answer
     * @return Answer string, or error message if generation fails
     */
    public static String generateWeatherAnswer(String weatherData, String question) {
        if (weatherData == null || weatherData.trim().isEmpty()) {
            Log.e(TAG, "Empty weather data provided");
            return "Unable to generate answer: weather data is missing.";
        }

        if (question == null || question.trim().isEmpty()) {
            Log.e(TAG, "Empty question provided");
            return "Please provide a valid question.";
        }

        try {
            String prompt = buildWeatherAnswerPrompt(weatherData, question);

            String result = null;
            if (!primaryKeyFailed && !API_KEY_PRIMARY.isEmpty()) {
                result = generateTextWithKey(API_KEY_PRIMARY, prompt, "PRIMARY");
                if (result != null) {
                    return result.trim();
                }
                primaryKeyFailed = true;
                Log.w(TAG, "Primary key failed for weather answer, trying backup");
            }

            if (!API_KEY_BACKUP.isEmpty()) {
                result = generateTextWithKey(API_KEY_BACKUP, prompt, "BACKUP");
                if (result != null) {
                    return result.trim();
                }
            }

            Log.e(TAG, "Both API keys failed for weather answer");
            return "Unable to generate answer at this time. Please try again later.";

        } catch (Exception e) {
            Log.e(TAG, "Error generating weather answer: " + e.getMessage(), e);
            return "An error occurred while generating the answer. Please try again.";
        }
    }

    /**
     * Generate weather questions asynchronously with callback.
     *
     * @param weatherData String containing weather information
     * @param callback    Callback to receive the generated questions
     */
    public static void generateWeatherQuestionsAsync(String weatherData, WeatherQuestionsCallback callback) {
        new Thread(() -> {
            try {
                String[] questions = generateWeatherQuestions(weatherData);
                postCallback(() -> callback.onQuestionsGenerated(questions));
            } catch (Exception e) {
                Log.e(TAG, "Async weather questions generation error: " + e.getMessage(), e);
                postCallback(() -> callback.onQuestionsGenerated(
                        new String[]{"What should I wear today?", "What should I prepare for an outdoor event today?"}));
            }
        }).start();
    }

    /**
     * Generate weather answer asynchronously with callback.
     *
     * @param weatherData String containing weather information
     * @param question    The question to answer
     * @param callback    Callback to receive the generated answer
     */
    public static void generateWeatherAnswerAsync(String weatherData, String question, WeatherAnswerCallback callback) {
        new Thread(() -> {
            try {
                String answer = generateWeatherAnswer(weatherData, question);
                postCallback(() -> callback.onAnswerGenerated(answer));
            } catch (Exception e) {
                Log.e(TAG, "Async weather answer generation error: " + e.getMessage(), e);
                postCallback(() -> callback.onAnswerGenerated(
                        "An error occurred while generating the answer. Please try again."));
            }
        }).start();
    }

    /**
     * Call the Gemini API with a specific key to generate text (non-JSON response).
     *
     * @param apiKey   The API key to use
     * @param prompt   The prompt to send
     * @param keyLabel Label for logging purposes
     * @return Generated text, or null if generation fails
     */
    private static String generateTextWithKey(String apiKey, String prompt, String keyLabel) {
        try {
            Log.d(TAG, "Generating text with " + keyLabel + " key");

            GenerativeModel model = new GenerativeModel(MODEL_NAME, apiKey);
            GenerativeModelFutures futures = GenerativeModelFutures.from(model);

            Content content = new Content.Builder()
                    .addText(prompt)
                    .build();

            ListenableFuture<GenerateContentResponse> response = futures.generateContent(content);
            GenerateContentResponse result = response.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            String text = result.getText();
            if (text != null && !text.isEmpty()) {
                Log.d(TAG, keyLabel + " key succeeded for text generation");
                return text;
            }

            Log.e(TAG, keyLabel + " key returned empty response");
            return null;

        } catch (java.util.concurrent.TimeoutException e) {
            Log.e(TAG, keyLabel + " timeout after " + TIMEOUT_SECONDS + "s");
            return null;
        } catch (Exception e) {
            Log.e(TAG, keyLabel + " error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Build the prompt for generating weather-related questions.
     *
     * @param weatherData String containing weather information
     * @return Formatted prompt string
     */
    private static String buildWeatherQuestionsPrompt(String weatherData) {
        return "Today's weather is: " + weatherData + "\n\n" +
                "Please generate exactly 2-3 context-specific questions based on the given weather data " +
                "that users might ask to help them make decisions about their day. " +
                "The questions should be practical and relevant to the current weather conditions. " +
                "Examples of good questions: 'What should I wear today?', 'What should I prepare for an outdoor event today?', " +
                "'Is it safe to go outside?', 'What activities are suitable for this weather?', etc.\n\n" +
                "Return ONLY the questions, one per line, without numbering or bullet points. " +
                "Each question should be on its own line. Do not include any additional text or explanations.";
    }

    /**
     * Build the prompt for generating weather-related answers.
     *
     * @param weatherData String containing weather information
     * @param question    The question to answer
     * @return Formatted prompt string
     */
    private static String buildWeatherAnswerPrompt(String weatherData, String question) {
        return "Today's weather is: " + weatherData + "\n\n" +
                "Question: " + question + "\n\n" +
                "Please provide a helpful, practical answer to this question based on the current weather conditions. " +
                "Be specific and actionable. Keep your answer concise (2-4 sentences).";
    }

    /**
     * Parse questions from LLM response text.
     * Extracts questions from text that may contain multiple lines.
     *
     * @param text The text response from LLM
     * @return Array of questions
     */
    static String[] parseQuestions(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new String[]{"What should I wear today?", "What should I prepare for an outdoor event today?"};
        }

        // Split by newlines and filter out empty lines
        String[] lines = text.split("\n");
        java.util.ArrayList<String> questions = new java.util.ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            // Remove numbering, bullets, dashes, etc.
            line = line.replaceAll("^[\\d\\.\\-\\*\\+\\s]+", "").trim();
            if (!line.isEmpty() && line.length() > 5) { // Minimum question length
                questions.add(line);
            }
        }

        // Ensure we have at least 2 questions
        if (questions.size() < 2) {
            questions.clear();
            questions.add("What should I wear today?");
            questions.add("What should I prepare for an outdoor event today?");
        }

        // Limit to 3 questions max
        if (questions.size() > 3) {
            questions = new java.util.ArrayList<>(questions.subList(0, 3));
        }

        return questions.toArray(new String[0]);
    }

    /**
     * Callback interface for async theme generation.
     */
    public interface ThemeCallback {
        void onThemeGenerated(ThemeSpec spec);
    }

    /**
     * Callback interface for async weather questions generation.
     */
    public interface WeatherQuestionsCallback {
        void onQuestionsGenerated(String[] questions);
    }

    /**
     * Callback interface for async weather answer generation.
     */
    public interface WeatherAnswerCallback {
        void onAnswerGenerated(String answer);
    }
}
