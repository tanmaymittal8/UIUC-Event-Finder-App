package edu.uiuc.cs427app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for UI theme functionality.
 * Verifies theme creation, validation, parsing, and color specifications.
 */
public class ThemeUITest {

    /**
     * Test that ThemeSpec correctly stores all 5 color values.
     */
    @Test
    public void testThemeSpecStoresAllColors() {
        ThemeSpec spec = new ThemeSpec();
        spec.backgroundHex = "#FFFFFF";
        spec.textHex = "#000000";
        spec.accentHex = "#FF5733";
        spec.buttonHex = "#1976D2";
        spec.secondaryHex = "#F5F5F5";

        assertEquals("Background should match", "#FFFFFF", spec.backgroundHex);
        assertEquals("Text should match", "#000000", spec.textHex);
        assertEquals("Accent should match", "#FF5733", spec.accentHex);
        assertEquals("Button should match", "#1976D2", spec.buttonHex);
        assertEquals("Secondary should match", "#F5F5F5", spec.secondaryHex);
    }

    /**
     * Test that default theme has valid hex colors.
     */
    @Test
    public void testDefaultThemeHasValidColors() {
        ThemeSpec defaultTheme = ThemeSpec.defaultLight();

        assertNotNull("Default theme should exist", defaultTheme);
        assertTrue("Background should be valid hex",
                defaultTheme.backgroundHex.matches("^#[0-9A-Fa-f]{6}$"));
        assertTrue("Text should be valid hex",
                defaultTheme.textHex.matches("^#[0-9A-Fa-f]{6}$"));
        assertTrue("Accent should be valid hex",
                defaultTheme.accentHex.matches("^#[0-9A-Fa-f]{6}$"));
        assertTrue("Button should be valid hex",
                defaultTheme.buttonHex.matches("^#[0-9A-Fa-f]{6}$"));
        assertTrue("Secondary should be valid hex",
                defaultTheme.secondaryHex.matches("^#[0-9A-Fa-f]{6}$"));
    }

    /**
     * Test JSON serialization of ThemeSpec.
     */
    @Test
    public void testThemeSpecToJson() {
        ThemeSpec spec = new ThemeSpec();
        spec.backgroundHex = "#FFFFFF";
        spec.textHex = "#000000";
        spec.accentHex = "#FF5733";
        spec.buttonHex = "#1976D2";
        spec.secondaryHex = "#F5F5F5";

        String json = spec.toJson();

        assertNotNull("JSON should not be null", json);
        assertTrue("JSON should contain background", json.contains("background"));
        assertTrue("JSON should contain text", json.contains("text"));
        assertTrue("JSON should contain accent", json.contains("accent"));
        assertTrue("JSON should contain button", json.contains("button"));
        assertTrue("JSON should contain secondary", json.contains("secondary"));
        assertTrue("JSON should contain #FFFFFF", json.contains("#FFFFFF"));
        assertTrue("JSON should contain #000000", json.contains("#000000"));
    }

    /**
     * Test JSON deserialization of ThemeSpec.
     */
    @Test
    public void testThemeSpecFromJson() {
        String json = "{\"background\":\"#FFFFFF\",\"text\":\"#000000\"," +
                "\"accent\":\"#FF5733\",\"button\":\"#1976D2\"," +
                "\"secondary\":\"#F5F5F5\"}";

        ThemeSpec spec = ThemeSpec.fromJson(json);

        assertNotNull("Spec should be created from JSON", spec);
        assertEquals("Background should match", "#FFFFFF", spec.backgroundHex);
        assertEquals("Text should match", "#000000", spec.textHex);
        assertEquals("Accent should match", "#FF5733", spec.accentHex);
        assertEquals("Button should match", "#1976D2", spec.buttonHex);
        assertEquals("Secondary should match", "#F5F5F5", spec.secondaryHex);
    }

    /**
     * Test that ThemeSpec validates correctly with valid colors.
     */
    @Test
    public void testThemeSpecValidationWithValidColors() {
        ThemeSpec spec = new ThemeSpec();
        spec.backgroundHex = "#FFFFFF";
        spec.textHex = "#000000";

        assertTrue("Valid theme should pass validation", spec.isValid());
    }

    /**
     * Test that ThemeSpec validation fails with invalid colors.
     */
    @Test
    public void testThemeSpecValidationWithInvalidColors() {
        ThemeSpec spec = new ThemeSpec();
        spec.backgroundHex = "not-a-color";
        spec.textHex = "#000000";

        assertFalse("Invalid background should fail validation", spec.isValid());

        spec.backgroundHex = "#FFFFFF";
        spec.textHex = "invalid";

        assertFalse("Invalid text should fail validation", spec.isValid());
    }

    /**
     * Test that ThemeSpec validation fails with null colors.
     */
    @Test
    public void testThemeSpecValidationWithNullColors() {
        ThemeSpec spec = new ThemeSpec();
        spec.backgroundHex = null;
        spec.textHex = "#000000";

        assertFalse("Null background should fail validation", spec.isValid());

        spec.backgroundHex = "#FFFFFF";
        spec.textHex = null;

        assertFalse("Null text should fail validation", spec.isValid());
    }

    /**
     * Test round-trip JSON serialization/deserialization.
     */
    @Test
    public void testThemeSpecJsonRoundTrip() {
        ThemeSpec original = new ThemeSpec();
        original.backgroundHex = "#123456";
        original.textHex = "#ABCDEF";
        original.accentHex = "#FF0000";
        original.buttonHex = "#00FF00";
        original.secondaryHex = "#0000FF";

        String json = original.toJson();
        ThemeSpec restored = ThemeSpec.fromJson(json);

        assertEquals("Background should survive round-trip",
                original.backgroundHex, restored.backgroundHex);
        assertEquals("Text should survive round-trip",
                original.textHex, restored.textHex);
        assertEquals("Accent should survive round-trip",
                original.accentHex, restored.accentHex);
        assertEquals("Button should survive round-trip",
                original.buttonHex, restored.buttonHex);
        assertEquals("Secondary should survive round-trip",
                original.secondaryHex, restored.secondaryHex);
    }

    /**
     * Test that malformed JSON returns default theme.
     */
    @Test
    public void testMalformedJsonReturnsDefault() {
        String malformed = "{this is not valid json}";
        ThemeSpec spec = ThemeSpec.fromJson(malformed);
        ThemeSpec defaultSpec = ThemeSpec.defaultLight();

        assertNotNull("Should return a spec", spec);
        assertEquals("Should return default background",
                defaultSpec.backgroundHex, spec.backgroundHex);
        assertEquals("Should return default text",
                defaultSpec.textHex, spec.textHex);
    }

    /**
     * Test that empty JSON returns default theme.
     */
    @Test
    public void testEmptyJsonReturnsDefault() {
        ThemeSpec spec = ThemeSpec.fromJson("");
        ThemeSpec defaultSpec = ThemeSpec.defaultLight();

        assertNotNull("Should return a spec", spec);
        assertEquals("Should return default background",
                defaultSpec.backgroundHex, spec.backgroundHex);
    }

    /**
     * Test that null JSON returns default theme.
     */
    @Test
    public void testNullJsonReturnsDefault() {
        ThemeSpec spec = ThemeSpec.fromJson(null);
        ThemeSpec defaultSpec = ThemeSpec.defaultLight();

        assertNotNull("Should return a spec", spec);
        assertEquals("Should return default background",
                defaultSpec.backgroundHex, spec.backgroundHex);
    }

    /**
     * Test JSON with partial data uses defaults for missing fields.
     */
    @Test
    public void testPartialJsonUsesDefaults() {
        String partialJson = "{\"background\":\"#FFFFFF\",\"text\":\"#000000\"}";
        ThemeSpec spec = ThemeSpec.fromJson(partialJson);

        assertNotNull("Spec should be created", spec);
        assertEquals("Background should be from JSON", "#FFFFFF", spec.backgroundHex);
        assertEquals("Text should be from JSON", "#000000", spec.textHex);
        assertNotNull("Accent should have default", spec.accentHex);
        assertNotNull("Button should have default", spec.buttonHex);
        assertNotNull("Secondary should have default", spec.secondaryHex);
    }

    /**
     * Test that hex color validation accepts uppercase.
     */
    @Test
    public void testHexValidationAcceptsUppercase() {
        ThemeSpec spec = new ThemeSpec();
        spec.backgroundHex = "#FFFFFF";
        spec.textHex = "#000000";

        assertTrue("Uppercase hex should be valid", spec.isValid());
    }

    /**
     * Test that hex color validation accepts lowercase.
     */
    @Test
    public void testHexValidationAcceptsLowercase() {
        ThemeSpec spec = new ThemeSpec();
        spec.backgroundHex = "#ffffff";
        spec.textHex = "#000000";

        assertTrue("Lowercase hex should be valid", spec.isValid());
    }

    /**
     * Test that hex color validation accepts mixed case.
     */
    @Test
    public void testHexValidationAcceptsMixedCase() {
        ThemeSpec spec = new ThemeSpec();
        spec.backgroundHex = "#FfFfFf";
        spec.textHex = "#aAbBcC";

        assertTrue("Mixed case hex should be valid", spec.isValid());
    }

    /**
     * Test that 3-digit hex codes are rejected.
     */
    @Test
    public void testHexValidationRejectsShortCodes() {
        ThemeSpec spec = new ThemeSpec();
        spec.backgroundHex = "#FFF";
        spec.textHex = "#000000";

        assertFalse("3-digit hex should be invalid", spec.isValid());
    }

    /**
     * Test that hex codes without # are rejected.
     */
    @Test
    public void testHexValidationRequiresHashSymbol() {
        ThemeSpec spec = new ThemeSpec();
        spec.backgroundHex = "FFFFFF";
        spec.textHex = "#000000";

        assertFalse("Hex without # should be invalid", spec.isValid());
    }

    /**
     * Test minimum requirements are met (background + text).
     * REQUIREMENT: Must have at least 2 colors in hex format.
     */
    @Test
    public void testMinimumRequirementColors() {
        ThemeSpec spec = ThemeSpec.fromJson(
                "{\"background\":\"#FFFFFF\",\"text\":\"#000000\"}"
        );

        assertNotNull("Spec should exist", spec);
        assertNotNull("Background (required) must exist", spec.backgroundHex);
        assertNotNull("Text (required) must exist", spec.textHex);
        assertTrue("Background must be hex",
                spec.backgroundHex.matches("^#[0-9A-Fa-f]{6}$"));
        assertTrue("Text must be hex",
                spec.textHex.matches("^#[0-9A-Fa-f]{6}$"));
        assertTrue("Must be valid", spec.isValid());
    }

    /**
     * Test that extra colors beyond minimum are preserved.
     * Implementation exceeds minimum requirement of 2 colors.
     */
    @Test
    public void testExtraColorsPreserved() {
        ThemeSpec spec = ThemeSpec.fromJson(
                "{\"background\":\"#111111\",\"text\":\"#222222\"," +
                        "\"accent\":\"#333333\",\"button\":\"#444444\",\"secondary\":\"#555555\"}"
        );

        assertEquals("All 5 colors should be preserved", "#111111", spec.backgroundHex);
        assertEquals("All 5 colors should be preserved", "#222222", spec.textHex);
        assertEquals("All 5 colors should be preserved", "#333333", spec.accentHex);
        assertEquals("All 5 colors should be preserved", "#444444", spec.buttonHex);
        assertEquals("All 5 colors should be preserved", "#555555", spec.secondaryHex);
    }

    /**
     * Test that different theme descriptions produce different valid themes.
     */
    @Test
    public void testDifferentThemesAreDistinct() {
        ThemeSpec theme1 = ThemeSpec.fromJson(
                "{\"background\":\"#FFFFFF\",\"text\":\"#000000\"," +
                        "\"accent\":\"#FF0000\",\"button\":\"#00FF00\",\"secondary\":\"#0000FF\"}"
        );

        ThemeSpec theme2 = ThemeSpec.fromJson(
                "{\"background\":\"#000000\",\"text\":\"#FFFFFF\"," +
                        "\"accent\":\"#00FF00\",\"button\":\"#0000FF\",\"secondary\":\"#FF0000\"}"
        );

        assertNotEquals("Different themes should have different backgrounds",
                theme1.backgroundHex, theme2.backgroundHex);
        assertNotEquals("Different themes should have different text colors",
                theme1.textHex, theme2.textHex);
        assertTrue("Both themes should be valid",
                theme1.isValid() && theme2.isValid());
    }
}

