package edu.uiuc.cs427app;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Integration tests for LLM-driven weather insights feature.
 * Tests verify that the Gemini API generates valid weather-related questions
 * and answers based on weather data.
 * <p>
 * Uses gemini-2.5-flash-lite model via the official Gemini SDK.
 * <p>
 * Note: Requires valid API keys in local.properties.
 * If keys are missing/invalid, tests verify fallback behavior.
 */
public class WeatherInsightsLLMTest {

    /**
     * Tests that generateWeatherQuestions returns valid questions array.
     * Verifies minimum requirements: at least 2 questions, at most 3.
     */
    @Test
    public void testGenerateWeatherQuestionsReturnsValidArray() {
        String weatherData = "Temperature: 72°F, Condition: Partly Cloudy, Humidity: 65%, Wind: 10 mph";
        String[] questions = LLMClient.generateWeatherQuestions(weatherData);

        assertNotNull("Questions array should not be null", questions);
        assertTrue("Should have at least 2 questions", questions.length >= 2);
        assertTrue("Should have at most 3 questions", questions.length <= 3);

        for (String question : questions) {
            assertNotNull("Question should not be null", question);
            assertTrue("Question should not be empty", !question.trim().isEmpty());
            assertTrue("Question should be at least 5 characters", question.length() >= 5);
        }
    }

    /**
     * Tests that empty weather data returns default questions.
     */
    @Test
    public void testEmptyWeatherDataReturnsDefaults() {
        String[] questions = LLMClient.generateWeatherQuestions("");
        assertNotNull("Should return default questions", questions);
        assertTrue("Should have at least 2 default questions", questions.length >= 2);
    }

    /**
     * Tests that null weather data returns default questions.
     */
    @Test
    public void testNullWeatherDataReturnsDefaults() {
        String[] questions = LLMClient.generateWeatherQuestions(null);
        assertNotNull("Should return default questions", questions);
        assertTrue("Should have at least 2 default questions", questions.length >= 2);
    }

    /**
     * Tests async generation of weather questions with callback.
     */
    @Test(timeout = 30000)
    public void testGenerateWeatherQuestionsAsync() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String[]> resultQuestions = new AtomicReference<>();

        String weatherData = "Temperature: 70°F, Condition: Cloudy, Humidity: 60%, Wind: 8 mph";
        LLMClient.generateWeatherQuestionsAsync(weatherData, questions -> {
            resultQuestions.set(questions);
            latch.countDown();
        });

        assertTrue("Async operation should complete", latch.await(25, TimeUnit.SECONDS));

        String[] questions = resultQuestions.get();
        assertNotNull("Callback should receive questions", questions);
        assertTrue("Should have at least 2 questions", questions.length >= 2);
    }

    /**
     * Tests that generateWeatherAnswer returns a non-empty answer.
     */
    @Test
    public void testGenerateWeatherAnswerReturnsValidAnswer() {
        String weatherData = "Temperature: 68°F, Condition: Partly Cloudy, Humidity: 70%, Wind: 12 mph";
        String question = "What should I wear today?";
        String answer = LLMClient.generateWeatherAnswer(weatherData, question);

        assertNotNull("Answer should not be null", answer);
        assertTrue("Answer should not be empty", !answer.trim().isEmpty());
    }

    /**
     * Tests that empty weather data returns error message.
     */
    @Test
    public void testGenerateAnswerWithEmptyWeatherData() {
        String answer = LLMClient.generateWeatherAnswer("", "What should I wear?");
        assertNotNull("Should return error message", answer);
        assertTrue("Should not be empty", !answer.trim().isEmpty());
    }

    /**
     * Tests that null weather data returns error message.
     */
    @Test
    public void testGenerateAnswerWithNullWeatherData() {
        String answer = LLMClient.generateWeatherAnswer(null, "What should I wear?");
        assertNotNull("Should return error message", answer);
        assertTrue("Should not be empty", !answer.trim().isEmpty());
    }

    /**
     * Tests that empty question returns error message.
     */
    @Test
    public void testGenerateAnswerWithEmptyQuestion() {
        String weatherData = "Temperature: 70°F, Condition: Sunny, Humidity: 50%, Wind: 10 mph";
        String answer = LLMClient.generateWeatherAnswer(weatherData, "");
        assertNotNull("Should return error message", answer);
        assertTrue("Should not be empty", !answer.trim().isEmpty());
    }

    /**
     * Tests that null question returns error message.
     */
    @Test
    public void testGenerateAnswerWithNullQuestion() {
        String weatherData = "Temperature: 70°F, Condition: Sunny, Humidity: 50%, Wind: 10 mph";
        String answer = LLMClient.generateWeatherAnswer(weatherData, null);
        assertNotNull("Should return error message", answer);
        assertTrue("Should not be empty", !answer.trim().isEmpty());
    }

    /**
     * Tests async generation of weather answer with callback.
     */
    @Test(timeout = 30000)
    public void testGenerateWeatherAnswerAsync() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> resultAnswer = new AtomicReference<>();

        String weatherData = "Temperature: 75°F, Condition: Clear, Humidity: 55%, Wind: 7 mph";
        String question = "What activities are suitable for this weather?";

        LLMClient.generateWeatherAnswerAsync(weatherData, question, answer -> {
            resultAnswer.set(answer);
            latch.countDown();
        });

        assertTrue("Async operation should complete", latch.await(25, TimeUnit.SECONDS));

        String answer = resultAnswer.get();
        assertNotNull("Callback should receive an answer", answer);
        assertTrue("Answer should not be empty", !answer.trim().isEmpty());
    }
}

