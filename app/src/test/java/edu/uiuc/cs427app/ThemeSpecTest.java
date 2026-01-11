package edu.uiuc.cs427app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for ThemeSpec JSON parsing and validation
 */
public class ThemeSpecTest {

    /**
     * Tests parsing of valid JSON containing all theme fields.
     * Verifies that all color values are correctly extracted and assigned to ThemeSpec properties.
     */
    @Test
    public void testValidJsonParsing() {
        String json = "{\"background\":\"#FFFFFF\",\"text\":\"#000000\",\"accent\":\"#FF5733\",\"button\":\"#1976D2\",\"secondary\":\"#F5F5F5\"}";
        ThemeSpec spec = ThemeSpec.fromJson(json);

        assertNotNull(spec);
        assertEquals("#FFFFFF", spec.backgroundHex);
        assertEquals("#000000", spec.textHex);
        assertEquals("#FF5733", spec.accentHex);
        assertEquals("#1976D2", spec.buttonHex);
        assertEquals("#F5F5F5", spec.secondaryHex);
    }

    /**
     * Tests parsing of minimal JSON with only required fields (background and text).
     * Optional fields should automatically receive default values.
     */
    @Test
    public void testMinimalJsonParsing() {
        // Only required fields
        String json = "{\"background\":\"#FFFFFF\",\"text\":\"#000000\"}";
        ThemeSpec spec = ThemeSpec.fromJson(json);

        assertNotNull(spec);
        assertEquals("#FFFFFF", spec.backgroundHex);
        assertEquals("#000000", spec.textHex);
        // Optional fields should have defaults
        assertNotNull(spec.accentHex);
        assertNotNull(spec.buttonHex);
        assertNotNull(spec.secondaryHex);
    }

    /**
     * Tests that invalid JSON syntax falls back to default theme.
     * Ensures the app doesn't crash when receiving malformed JSON.
     */
    @Test
    public void testInvalidJsonFallsBackToDefault() {
        String invalidJson = "{this is not valid json}";
        ThemeSpec spec = ThemeSpec.fromJson(invalidJson);

        assertNotNull(spec);
        // Should return default theme
        ThemeSpec defaultSpec = ThemeSpec.defaultLight();
        assertEquals(defaultSpec.backgroundHex, spec.backgroundHex);
        assertEquals(defaultSpec.textHex, spec.textHex);
    }

    /**
     * Tests that empty string input falls back to default theme.
     * Handles edge case where JSON string is empty.
     */
    @Test
    public void testEmptyJsonFallsBackToDefault() {
        ThemeSpec spec = ThemeSpec.fromJson("");

        assertNotNull(spec);
        ThemeSpec defaultSpec = ThemeSpec.defaultLight();
        assertEquals(defaultSpec.backgroundHex, spec.backgroundHex);
    }

    /**
     * Tests that null input falls back to default theme.
     * Ensures robustness when JSON parameter is null.
     */
    @Test
    public void testNullJsonFallsBackToDefault() {
        ThemeSpec spec = ThemeSpec.fromJson(null);

        assertNotNull(spec);
        ThemeSpec defaultSpec = ThemeSpec.defaultLight();
        assertEquals(defaultSpec.backgroundHex, spec.backgroundHex);
    }

    /**
     * Tests conversion of ThemeSpec object to JSON string.
     * Verifies that all theme properties are correctly serialized.
     */
    @Test
    public void testToJson() {
        ThemeSpec spec = new ThemeSpec();
        spec.backgroundHex = "#123456";
        spec.textHex = "#ABCDEF";
        spec.accentHex = "#FF0000";
        spec.buttonHex = "#00FF00";
        spec.secondaryHex = "#0000FF";

        String json = spec.toJson();

        assertNotNull(json);
        assertTrue(json.contains("\"background\":\"#123456\""));
        assertTrue(json.contains("\"text\":\"#ABCDEF\""));
        assertTrue(json.contains("\"accent\":\"#FF0000\""));
        assertTrue(json.contains("\"button\":\"#00FF00\""));
        assertTrue(json.contains("\"secondary\":\"#0000FF\""));
    }

    /**
     * Tests round-trip conversion: ThemeSpec → JSON → ThemeSpec.
     * Ensures data integrity is maintained through serialization and deserialization.
     */
    @Test
    public void testRoundTripJsonConversion() {
        // Create spec, convert to JSON, parse back
        ThemeSpec original = new ThemeSpec();
        original.backgroundHex = "#AABBCC";
        original.textHex = "#112233";

        String json = original.toJson();
        ThemeSpec parsed = ThemeSpec.fromJson(json);

        assertEquals(original.backgroundHex, parsed.backgroundHex);
        assertEquals(original.textHex, parsed.textHex);
    }

    /**
     * Tests validation of properly formatted hex color codes.
     * Valid colors should pass the isValid() check.
     */
    @Test
    public void testValidHexColorValidation() {
        ThemeSpec spec = new ThemeSpec();
        spec.backgroundHex = "#FFFFFF";
        spec.textHex = "#000000";

        assertTrue(spec.isValid());
    }

    /**
     * Tests validation rejection of hex colors missing the # prefix.
     * Improperly formatted colors should fail validation.
     */
    @Test
    public void testInvalidHexColorValidation() {
        ThemeSpec spec = new ThemeSpec();
        spec.backgroundHex = "FFFFFF"; // Missing #
        spec.textHex = "#000000";

        assertFalse(spec.isValid());
    }

    /**
     * Tests validation rejection of short-form hex colors (#FFF instead of #FFFFFF).
     * Only 6-digit hex codes should be accepted.
     */
    @Test
    public void testShortHexColorValidation() {
        ThemeSpec spec = new ThemeSpec();
        spec.backgroundHex = "#FFF"; // Too short
        spec.textHex = "#000000";

        assertFalse(spec.isValid());
    }

    /**
     * Tests that the default light theme is properly configured.
     * All fields should be non-null and valid.
     */
    @Test
    public void testDefaultLightTheme() {
        ThemeSpec spec = ThemeSpec.defaultLight();

        assertNotNull(spec);
        assertNotNull(spec.backgroundHex);
        assertNotNull(spec.textHex);
        assertNotNull(spec.accentHex);
        assertNotNull(spec.buttonHex);
        assertNotNull(spec.secondaryHex);
        assertTrue(spec.isValid());
    }

    /**
     * Tests parsing and validation of lowercase hex color codes.
     * Both uppercase and lowercase hex codes should be accepted.
     */
    @Test
    public void testLowercaseHexColors() {
        String json = "{\"background\":\"#ffffff\",\"text\":\"#000000\"}";
        ThemeSpec spec = ThemeSpec.fromJson(json);

        assertNotNull(spec);
        assertEquals("#ffffff", spec.backgroundHex);
        assertTrue(spec.isValid());
    }

    /**
     * Tests parsing and validation of mixed-case hex color codes.
     * Hex codes with mixed upper and lowercase should be accepted.
     */
    @Test
    public void testMixedCaseHexColors() {
        String json = "{\"background\":\"#FfFfFf\",\"text\":\"#000000\"}";
        ThemeSpec spec = ThemeSpec.fromJson(json);

        assertNotNull(spec);
        assertEquals("#FfFfFf", spec.backgroundHex);
        assertTrue(spec.isValid());
    }

    /**
     * Tests parsing of JSON with some optional fields missing.
     * Missing optional fields should use default values.
     */
    @Test
    public void testPartialJsonWithMissingFields() {
        // Missing button and secondary
        String json = "{\"background\":\"#FFFFFF\",\"text\":\"#000000\",\"accent\":\"#FF0000\"}";
        ThemeSpec spec = ThemeSpec.fromJson(json);

        assertNotNull(spec);
        assertEquals("#FFFFFF", spec.backgroundHex);
        assertEquals("#000000", spec.textHex);
        assertEquals("#FF0000", spec.accentHex);
        // Should have default values for missing fields
        assertNotNull(spec.buttonHex);
        assertNotNull(spec.secondaryHex);
    }

    /**
     * Tests parsing of a cyberpunk-themed color scheme.
     * Validates that dark, neon colors are correctly parsed and validated.
     */
    @Test
    public void testCyberpunkThemeExample() {
        // Simulate a cyberpunk theme response
        String json = "{\"background\":\"#0a0e27\",\"text\":\"#00ffff\",\"accent\":\"#ff00ff\",\"button\":\"#7b2cbf\",\"secondary\":\"#1a1f3a\"}";
        ThemeSpec spec = ThemeSpec.fromJson(json);

        assertNotNull(spec);
        assertEquals("#0a0e27", spec.backgroundHex);
        assertEquals("#00ffff", spec.textHex);
        assertEquals("#ff00ff", spec.accentHex);
        assertTrue(spec.isValid());
    }

    /**
     * Tests parsing of a summer beach-themed color scheme.
     * Validates that warm, sandy colors are correctly parsed and validated.
     */
    @Test
    public void testSummerBeachThemeExample() {
        // Simulate a summer beach theme response
        String json = "{\"background\":\"#f4e4c1\",\"text\":\"#2c3e50\",\"accent\":\"#ff6b35\",\"button\":\"#1e90ff\",\"secondary\":\"#fff8dc\"}";
        ThemeSpec spec = ThemeSpec.fromJson(json);

        assertNotNull(spec);
        assertEquals("#f4e4c1", spec.backgroundHex);
        assertEquals("#2c3e50", spec.textHex);
        assertTrue(spec.isValid());
    }
}

