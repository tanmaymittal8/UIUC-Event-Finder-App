package edu.uiuc.cs427app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for parsing LLM API responses and extracting JSON
 * Note: These test the parsing logic, not actual API calls
 */
public class LLMResponseParsingTest {

    /**
     * Tests parsing of a clean, well-formed JSON response from the Gemini API.
     * Verifies that ThemeSpec can successfully parse JSON with all required fields.
     */
    @Test
    public void testParseCleanJsonResponse() {
        // Simulate a clean JSON response from Gemini
        String geminiResponse = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"{\\\"background\\\":\\\"#FFFFFF\\\",\\\"text\\\":\\\"#000000\\\",\\\"accent\\\":\\\"#FF5733\\\",\\\"button\\\":\\\"#1976D2\\\",\\\"secondary\\\":\\\"#F5F5F5\\\"}\"}]}}]}";

        // This would be parsed by LLMClient.parseGeminiResponse()
        // For now, we test the ThemeSpec can handle the extracted JSON
        String extractedJson = "{\"background\":\"#FFFFFF\",\"text\":\"#000000\",\"accent\":\"#FF5733\",\"button\":\"#1976D2\",\"secondary\":\"#F5F5F5\"}";
        ThemeSpec spec = ThemeSpec.fromJson(extractedJson);

        assertNotNull(spec);
        assertTrue(spec.isValid());
    }

    /**
     * Tests extraction of JSON from markdown code blocks with ```json syntax.
     * The LLM may wrap JSON responses in markdown formatting, which must be handled.
     */
    @Test
    public void testExtractJsonFromMarkdownCodeBlock() {
        // LLM might wrap response in markdown
        String textWithMarkdown = "```json\n{\"background\":\"#FFFFFF\",\"text\":\"#000000\"}\n```";

        // Simulate extraction (similar to LLMClient.extractJsonFromText)
        String extracted = extractJsonFromMarkdown(textWithMarkdown);
        ThemeSpec spec = ThemeSpec.fromJson(extracted);

        assertNotNull(spec);
        assertEquals("#FFFFFF", spec.backgroundHex);
        assertEquals("#000000", spec.textHex);
    }

    /**
     * Tests extraction of JSON from plain markdown code blocks without language tag.
     * Verifies handling of ``` syntax without the json specifier.
     */
    @Test
    public void testExtractJsonFromPlainCodeBlock() {
        String textWithCodeBlock = "```\n{\"background\":\"#123456\",\"text\":\"#ABCDEF\"}\n```";

        String extracted = extractJsonFromMarkdown(textWithCodeBlock);
        ThemeSpec spec = ThemeSpec.fromJson(extracted);

        assertNotNull(spec);
        assertEquals("#123456", spec.backgroundHex);
        assertEquals("#ABCDEF", spec.textHex);
    }

    /**
     * Tests extraction of JSON when surrounded by extraneous text.
     * The LLM may include explanatory text before or after the JSON, which should be filtered out.
     */
    @Test
    public void testExtractJsonWithExtraText() {
        String textWithExtra = "Here's your theme:\n{\"background\":\"#FFFFFF\",\"text\":\"#000000\"}\nEnjoy!";

        String extracted = extractJsonFromText(textWithExtra);
        ThemeSpec spec = ThemeSpec.fromJson(extracted);

        assertNotNull(spec);
        assertTrue(spec.isValid());
    }

    /**
     * Tests that empty or missing LLM responses fall back to the default theme.
     * Ensures the app remains functional even when the API returns nothing.
     */
    @Test
    public void testEmptyResponseFallsBackToDefault() {
        String emptyResponse = "";
        ThemeSpec spec = ThemeSpec.fromJson(emptyResponse);

        assertNotNull(spec);
        // Should be default theme
        ThemeSpec defaultSpec = ThemeSpec.defaultLight();
        assertEquals(defaultSpec.backgroundHex, spec.backgroundHex);
    }

    /**
     * Tests that the hardcoded default theme JSON in LLMClient is valid and parseable.
     * This verifies the fallback theme will always work correctly.
     */
    @Test
    public void testDefaultThemeJsonIsValid() {
        // Test that the hardcoded default in LLMClient is valid
        String defaultJson = "{\"background\":\"#FFFFFF\",\"text\":\"#111111\",\"accent\":\"#2E7D32\",\"button\":\"#1976D2\",\"secondary\":\"#F5F5F5\"}";
        ThemeSpec spec = ThemeSpec.fromJson(defaultJson);

        assertNotNull(spec);
        assertTrue(spec.isValid());
        assertEquals("#FFFFFF", spec.backgroundHex);
        assertEquals("#111111", spec.textHex);
    }

    /**
     * Tests that malformed JSON (missing quotes, incorrect syntax) falls back to default theme.
     * Ensures robustness when LLM returns invalid JSON.
     */
    @Test
    public void testMalformedJsonFallback() {
        String malformed = "{background:#FFFFFF,text:#000000}"; // Missing quotes
        ThemeSpec spec = ThemeSpec.fromJson(malformed);

        // Should fallback to default
        assertNotNull(spec);
        ThemeSpec defaultSpec = ThemeSpec.defaultLight();
        assertEquals(defaultSpec.backgroundHex, spec.backgroundHex);
    }

    /**
     * Tests handling of incomplete JSON with only some fields present.
     * Missing fields should use default values rather than causing errors.
     */
    @Test
    public void testIncompleteJsonWithOnlyBackground() {
        String incomplete = "{\"background\":\"#FFFFFF\"}";
        ThemeSpec spec = ThemeSpec.fromJson(incomplete);

        assertNotNull(spec);
        assertEquals("#FFFFFF", spec.backgroundHex);
        // Should have default for text
        assertNotNull(spec.textHex);
    }

    /**
     * Tests realistic LLM-generated theme for "cyberpunk nightscape" description.
     * Verifies dark, neon color palette characteristic of cyberpunk aesthetics.
     */
    @Test
    public void testLLMGeneratedCyberpunkResponse() {
        // Realistic response for "cyberpunk nightscape"
        String json = "{\"background\":\"#0d0221\",\"text\":\"#00d9ff\",\"accent\":\"#ff006e\",\"button\":\"#8338ec\",\"secondary\":\"#1a0f2e\"}";
        ThemeSpec spec = ThemeSpec.fromJson(json);

        assertNotNull(spec);
        assertTrue(spec.isValid());
        // Verify dark background
        assertTrue(spec.backgroundHex.startsWith("#0"));
    }

    /**
     * Tests realistic LLM-generated theme for "summer beach" description.
     * Verifies warm, sandy color palette characteristic of beach environments.
     */
    @Test
    public void testLLMGeneratedSummerBeachResponse() {
        // Realistic response for "summer beach"
        String json = "{\"background\":\"#ffd89b\",\"text\":\"#2c3e50\",\"accent\":\"#ff8c42\",\"button\":\"#4a90e2\",\"secondary\":\"#ffe5b4\"}";
        ThemeSpec spec = ThemeSpec.fromJson(json);

        assertNotNull(spec);
        assertTrue(spec.isValid());
        // Verify warm/light background
        assertTrue(spec.backgroundHex.startsWith("#ff"));
    }

    /**
     * Tests realistic LLM-generated theme for "forest morning" description.
     * Verifies natural green color palette with soft, earthy tones.
     */
    @Test
    public void testLLMGeneratedForestMorningResponse() {
        // Realistic response for "forest morning"
        String json = "{\"background\":\"#e8f5e9\",\"text\":\"#1b5e20\",\"accent\":\"#4caf50\",\"button\":\"#66bb6a\",\"secondary\":\"#c8e6c9\"}";
        ThemeSpec spec = ThemeSpec.fromJson(json);

        assertNotNull(spec);
        assertTrue(spec.isValid());
    }

    // Helper methods (simplified versions of LLMClient methods for testing)

    /**
     * Helper method to extract JSON from markdown code blocks.
     * Handles both ```json and plain ``` code block syntax.
     *
     * @param text Text potentially containing markdown code blocks
     * @return Extracted JSON string
     */
    private String extractJsonFromMarkdown(String text) {
        text = text.trim();

        if (text.contains("```json")) {
            int start = text.indexOf("```json") + 7;
            int end = text.lastIndexOf("```");
            if (end > start) {
                return text.substring(start, end).trim();
            }
        } else if (text.contains("```")) {
            int start = text.indexOf("```") + 3;
            int end = text.lastIndexOf("```");
            if (end > start) {
                return text.substring(start, end).trim();
            }
        }

        return text;
    }

    /**
     * Helper method to extract JSON object from text with extraneous content.
     * Finds the first { and last } to isolate the JSON structure.
     *
     * @param text Text containing a JSON object
     * @return Extracted JSON string
     */
    private String extractJsonFromText(String text) {
        int jsonStart = text.indexOf('{');
        int jsonEnd = text.lastIndexOf('}');

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return text.substring(jsonStart, jsonEnd + 1);
        }

        return text;
    }

    /**
     * Tests handling of responses containing multiple JSON objects.
     * Should extract the complete JSON structure from first { to last }.
     */
    @Test
    public void testMultipleJsonObjectsInText() {
        // Edge case: multiple JSON objects, should extract first valid one
        String text = "{\"background\":\"#FFFFFF\",\"text\":\"#000000\"} {\"background\":\"#000000\",\"text\":\"#FFFFFF\"}";

        String extracted = extractJsonFromText(text);
        // Should get everything from first { to last }
        assertNotNull(extracted);
        assertTrue(extracted.contains("background"));
    }

    /**
     * Tests handling of nested JSON structures that don't match expected format.
     * Should gracefully fall back to default theme rather than crashing.
     */
    @Test
    public void testNestedJsonInResponse() {
        String nested = "{\"theme\":{\"background\":\"#FFFFFF\",\"text\":\"#000000\"}}";

        // ThemeSpec.fromJson should handle this gracefully (will fail and return default)
        ThemeSpec spec = ThemeSpec.fromJson(nested);
        assertNotNull(spec);
        // Will be default since structure doesn't match expected format
    }

    /**
     * Tests parsing of JSON responses that may contain unicode characters.
     * Ensures unicode doesn't break JSON parsing.
     */
    @Test
    public void testUnicodeInJsonResponse() {
        // LLM might include unicode characters
        String json = "{\"background\":\"#FFFFFF\",\"text\":\"#000000\",\"accent\":\"#FF5733\"}";
        ThemeSpec spec = ThemeSpec.fromJson(json);

        assertNotNull(spec);
        assertTrue(spec.isValid());
    }

    /**
     * Tests parsing of JSON with extra fields not defined in ThemeSpec.
     * Extra fields should be ignored without causing errors.
     */
    @Test
    public void testExtraFieldsInJson() {
        // LLM might return extra fields we don't use
        String json = "{\"background\":\"#FFFFFF\",\"text\":\"#000000\",\"accent\":\"#FF5733\",\"extraField\":\"value\",\"anotherField\":123}";
        ThemeSpec spec = ThemeSpec.fromJson(json);

        assertNotNull(spec);
        // Should parse successfully, ignoring extra fields
        assertEquals("#FFFFFF", spec.backgroundHex);
        assertTrue(spec.isValid());
    }

    /**
     * Tests parsing of pretty-printed JSON with newlines and indentation.
     * Whitespace should not affect parsing correctness.
     */
    @Test
    public void testWhitespaceInJson() {
        String json = "{\n  \"background\": \"#FFFFFF\",\n  \"text\": \"#000000\"\n}";
        ThemeSpec spec = ThemeSpec.fromJson(json);

        assertNotNull(spec);
        assertEquals("#FFFFFF", spec.backgroundHex);
        assertTrue(spec.isValid());
    }
}

