package edu.uiuc.cs427app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for parsing weather-related questions from LLM API responses.
 * Tests the parsing logic for extracting questions from text responses.
 * <p>
 * Note: These test the parsing logic, not actual API calls.
 */
public class WeatherInsightsParsingTest {

    /**
     * Tests parsing of clean, well-formed question responses.
     */
    @Test
    public void testParseCleanQuestionResponse() {
        String cleanResponse = "What should I wear today?\nWhat should I prepare for an outdoor event today?";
        String[] questions = parseQuestions(cleanResponse);

        assertNotNull("Questions should not be null", questions);
        assertEquals("Should have 2 questions", 2, questions.length);
        assertEquals("First question should match", "What should I wear today?", questions[0]);
        assertEquals("Second question should match", "What should I prepare for an outdoor event today?", questions[1]);
    }

    /**
     * Tests parsing of questions with numbering (1., 2., etc.).
     */
    @Test
    public void testParseQuestionsWithNumbering() {
        String numberedResponse = "1. What should I wear today?\n2. What should I prepare for an outdoor event today?\n3. Is it safe to go outside?";
        String[] questions = parseQuestions(numberedResponse);

        assertNotNull("Questions should not be null", questions);
        assertTrue("Should have at least 2 questions", questions.length >= 2);
        assertTrue("Should have at most 3 questions", questions.length <= 3);

        for (String question : questions) {
            assertTrue("Question should not start with number",
                    !question.trim().matches("^\\d+\\..*"));
        }
    }

    /**
     * Tests parsing of questions with bullet points.
     */
    @Test
    public void testParseQuestionsWithBullets() {
        String bulletResponse = "- What should I wear today?\n- What should I prepare for an outdoor event today?";
        String[] questions = parseQuestions(bulletResponse);

        assertNotNull("Questions should not be null", questions);
        assertTrue("Should have at least 2 questions", questions.length >= 2);

        for (String question : questions) {
            assertTrue("Question should not start with bullet",
                    !question.trim().startsWith("-") &&
                            !question.trim().startsWith("*") &&
                            !question.trim().startsWith("+"));
        }
    }

    /**
     * Tests parsing of empty response returns defaults.
     */
    @Test
    public void testParseEmptyResponseReturnsDefaults() {
        String[] questions = parseQuestions("");
        assertNotNull("Should return default questions", questions);
        assertTrue("Should have at least 2 default questions", questions.length >= 2);
    }

    /**
     * Tests parsing of null response returns defaults.
     */
    @Test
    public void testParseNullResponseReturnsDefaults() {
        String[] questions = parseQuestions(null);
        assertNotNull("Should return default questions", questions);
        assertTrue("Should have at least 2 default questions", questions.length >= 2);
    }

    /**
     * Tests parsing limits to maximum 3 questions.
     */
    @Test
    public void testParseMoreThanThreeQuestionsLimitsToThree() {
        String manyQuestions = "Question 1?\nQuestion 2?\nQuestion 3?\nQuestion 4?\nQuestion 5?";
        String[] questions = parseQuestions(manyQuestions);

        assertNotNull("Questions should not be null", questions);
        assertTrue("Should have at most 3 questions", questions.length <= 3);
    }

    /**
     * Tests parsing filters out short lines (less than 5 characters).
     */
    @Test
    public void testParseFiltersShortLines() {
        String responseWithShortLines = "What should I wear today?\nHi\nWhat should I prepare for an outdoor event today?\nOK";
        String[] questions = parseQuestions(responseWithShortLines);

        assertNotNull("Questions should not be null", questions);
        assertTrue("Should have at least 2 questions", questions.length >= 2);

        for (String question : questions) {
            assertTrue("Question should be at least 5 characters", question.length() >= 5);
        }
    }

    /**
     * Tests parsing ensures minimum 2 questions.
     */
    @Test
    public void testParseSingleQuestionReturnsAtLeastTwo() {
        String singleQuestion = "What should I wear today?";
        String[] questions = parseQuestions(singleQuestion);

        assertNotNull("Should return questions", questions);
        assertTrue("Should have at least 2 questions", questions.length >= 2);
    }

    /**
     * Helper method to call the package-private parseQuestions method.
     */
    private String[] parseQuestions(String text) {
        return LLMClient.parseQuestions(text);
    }
}

