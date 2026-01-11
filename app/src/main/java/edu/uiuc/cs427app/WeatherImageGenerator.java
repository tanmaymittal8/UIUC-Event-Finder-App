package edu.uiuc.cs427app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Weather-aware city image generator using Gemini 2.0 Flash Image via REST.
 * <p>
 * This mirrors the working curl pattern:
 * POST https://generativelanguage.googleapis.com/v1beta/models/
 * gemini-2.0-flash-preview-image-generation:generateContent
 * <p>
 * Body:
 * {
 * "generationConfig": { "responseModalities": ["TEXT","IMAGE"] },
 * "contents": [{ "parts": [{ "text": "<prompt>" }] }]
 * }
 * <p>
 * Then we decode candidates[0].content.parts[0].inlineData.data (base64)
 * into a Bitmap.
 */
public class WeatherImageGenerator {

    private static final String TAG = "WeatherImageGenerator";
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + "gemini-2.0-flash-preview-image-generation:generateContent";
    private static final String API_KEY_PRIMARY = BuildConfig.GEMINI_API_KEY_PRIMARY;
    private final ExecutorService executor;
    private final Handler mainHandler;
    public WeatherImageGenerator() {
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * High-level API: generate a city image given structured info.
     */
    public void generateCityImageAsync(
            @NonNull String cityName,
            @NonNull String stateOrRegion,
            @NonNull String country,
            @NonNull String weatherSummary,
            @NonNull String timeOfDay,
            @NonNull ImageCallback callback
    ) {
        String prompt = buildPrompt(cityName, stateOrRegion, country, weatherSummary, timeOfDay);
        generateFromPromptAsync(prompt, callback);
    }

    public void generateCityImageWithWeatherAsync(
            @NonNull String cityName,
            @NonNull String stateOrRegion,
            @NonNull String country,
            @NonNull WeatherInfo weatherData,
            @NonNull String timeOfDay,
            @NonNull ImageCallback callback
    ) {
        String prompt = buildEnhancedPrompt(cityName, stateOrRegion, country, weatherData, timeOfDay);
        generateFromPromptAsync(prompt, callback);
    }

    // ---------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------
    //prompt with weather data
    private String buildEnhancedPrompt(
            String cityName,
            String stateOrRegion,
            String country,
            WeatherInfo weather,
            String timeOfDay
    ) {
        if (weather == null) {

            return buildPrompt(cityName, stateOrRegion, country, "current local weather", timeOfDay);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Create a realistic photo of a city scene in ").append(cityName);

        if (stateOrRegion != null && !stateOrRegion.trim().isEmpty()) {
            sb.append(", ").append(stateOrRegion);
        }
        sb.append(", ").append(country).append("\n\n");

        // use weatehr data
        String weatherMain = weather.current.weather[0].main.toLowerCase();
        String weatherDesc = weather.current.weather[0].description;
        double temperature = weather.current.temp;
        int humidity = weather.current.humidity;
        double windSpeed = weather.current.wind_speed;

        sb.append("The scene should accurately reflect these current conditions:\n");
        sb.append("- Weather: ").append(weatherDesc).append("\n");
        sb.append("- Temperature: ").append(temperature).append("°F\n");
        sb.append("- Time of day: ").append(timeOfDay).append("\n");

        // prompt with different weather condition
        if (weatherMain.contains("rain") || weatherMain.contains("drizzle")) {
            sb.append("- Show wet streets, rain reflections, people with umbrellas or raincoats\n");
        } else if (weatherMain.contains("snow")) {
            sb.append("- Show snow-covered streets and buildings, people in winter clothing\n");
        } else if (weatherMain.contains("cloud")) {
            sb.append("- Show overcast sky, soft diffused lighting\n");
        } else if (weatherMain.contains("clear") || weatherMain.contains("sunny")) {
            if (timeOfDay.equals("daytime")) {
                sb.append("- Show bright sunlight, clear skies, strong shadows\n");
            }
        }
        if (temperature > 85) {
            sb.append("- Suggest hot weather: people in summer clothes, possible heat haze\n");
        } else if (temperature < 32) {
            sb.append("- Suggest freezing conditions: people bundled up, frost/ice visible\n");
        }
        if (windSpeed > 15) {
            sb.append("- Show wind effects: trees bending, flags waving, debris blowing\n");
        }
        if (humidity > 80) {
            sb.append("- Suggest high humidity: hazy atmosphere, possible fog/mist\n");
        }

        sb.append("\nCreate a photorealistic image that clearly conveys these specific ");
        sb.append("weather conditions. Do NOT include any text, words, or labels in the image.");

        return sb.toString();
    }

    //prompt without weather data
    private String buildPrompt(
            String cityName,
            String stateOrRegion,
            String country,
            String weatherSummary,
            String timeOfDay
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Create a realistic photo of a city scene.\n")
                .append("The scene should match this description:\n")
                .append("- City: ").append(cityName);

        if (stateOrRegion != null && !stateOrRegion.trim().isEmpty()) {
            sb.append(", ").append(stateOrRegion);
        }

        sb.append(", ").append(country).append("\n")
                .append("- Weather: ").append(weatherSummary).append("\n")
                .append("- Time of day: ").append(timeOfDay).append("\n\n")
                .append("Show typical city streets or skyline that clearly reflects the weather and ")
                .append("time of day. Do NOT include any text, words, or labels in the image.");

        return sb.toString();
    }

    private void generateFromPromptAsync(
            @NonNull String prompt,
            @NonNull ImageCallback callback
    ) {
        executor.submit(() -> {
            try {
                Bitmap bitmap = callGeminiImage(prompt);
                if (bitmap == null) {
                    throw new IllegalStateException("No image data found in Gemini response");
                }
                postSuccess(callback, bitmap);
            } catch (Throwable t) {
                Log.e(TAG, "Image generation failed: " + t.getMessage(), t);
                postError(callback, t);
            }
        });
    }

    /**
     * Performs the REST call to Gemini 2.0 Flash Image and returns a Bitmap.
     */
    private Bitmap callGeminiImage(String prompt) throws IOException, JSONException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(ENDPOINT);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // Headers: match curl example
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("x-goog-api-key", API_KEY_PRIMARY);

            // Build JSON body:
            // {
            //   "generationConfig": { "responseModalities": ["TEXT","IMAGE"] },
            //   "contents": [{ "parts": [{ "text": "<prompt>" }] }]
            // }
            JSONObject body = new JSONObject();

            JSONObject genConfig = new JSONObject();
            JSONArray modalities = new JSONArray();
            modalities.put("TEXT");
            modalities.put("IMAGE");
            genConfig.put("responseModalities", modalities);
            body.put("generationConfig", genConfig);

            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);

            JSONArray parts = new JSONArray();
            parts.put(textPart);

            JSONObject contentObj = new JSONObject();
            contentObj.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(contentObj);

            body.put("contents", contents);

            String jsonBody = body.toString();

            // Write request body
            try (OutputStream os = conn.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(
                         new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                writer.write(jsonBody);
                writer.flush();
            }

            int code = conn.getResponseCode();
            InputStream is;
            if (code >= 200 && code < 300) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
                if (is == null) {
                    throw new IOException("Gemini HTTP error " + code);
                }
            }

            String responseText = readStreamToString(is);

            if (code < 200 || code >= 300) {
                Log.e(TAG, "Gemini error response: " + responseText);
                throw new IOException("Gemini HTTP error " + code + ": " + responseText);
            }

            return extractBitmapFromResponse(responseText);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String readStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * Parses the JSON and extracts candidates[0].content.parts[0].inlineData.data.
     */
    private Bitmap extractBitmapFromResponse(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONArray candidates = root.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0) {
            throw new JSONException("No candidates in response");
        }

        JSONObject firstCandidate = candidates.getJSONObject(0);
        JSONObject content = firstCandidate.getJSONObject("content");
        JSONArray parts = content.getJSONArray("parts");

        for (int i = 0; i < parts.length(); i++) {
            JSONObject part = parts.getJSONObject(i);
            JSONObject inlineData = part.optJSONObject("inlineData");
            if (inlineData != null) {
                String b64 = inlineData.optString("data", null);
                if (b64 != null && !b64.isEmpty()) {
                    byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
                    return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                }
            }
        }

        throw new JSONException("No inlineData.data field with image found");
    }

    private void postSuccess(@NonNull ImageCallback callback, @NonNull Bitmap bitmap) {
        mainHandler.post(() -> callback.onImageReady(bitmap));
    }

    private void postError(@NonNull ImageCallback callback, @NonNull Throwable t) {
        mainHandler.post(() -> callback.onError(t));
    }

    /**
     * Callback for asynchronous image results.
     */
    public interface ImageCallback {
        void onImageReady(@NonNull Bitmap bitmap);

        void onError(@NonNull Throwable t);
    }

}
