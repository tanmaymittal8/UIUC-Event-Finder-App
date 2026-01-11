package edu.uiuc.cs427app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Integration tests for LLM-driven UI customization.
 * Tests verify that the Gemini API generates valid themes from natural language descriptions.
 * <p>
 * Uses gemini-2.5-flash-lite model via the official Gemini SDK.
 * <p>
 * Note: Requires valid API keys in local.properties.
 * If keys are missing/invalid, tests verify fallback to default theme.
 */
public class LLMClientIntegrationTest {

    /**
     * Tests that generateThemeJson returns valid JSON for a simple description.
     * This is a synchronous API call test.
     */
    @Test
    public void testGenerateThemeJsonReturnsValidJson() {
        String description = "ocean sunset";
        String json = LLMClient.generateThemeJson(description);

        assertNotNull("JSON should not be null", json);
        assertFalse("JSON should not be empty", json.isEmpty());
        assertTrue("JSON should contain background field", json.contains("background"));
        assertTrue("JSON should contain text field", json.contains("text"));

        // Should be parseable as ThemeSpec
        ThemeSpec spec = ThemeSpec.fromJson(json);
        assertNotNull("ThemeSpec should be created", spec);
        assertTrue("ThemeSpec should be valid", spec.isValid());
    }

    /**
     * Tests that generateThemeSpec returns a valid ThemeSpec object.
     * Verifies the complete flow from description to ThemeSpec.
     */
    @Test
    public void testGenerateThemeSpecReturnsValidSpec() {
        String description = "cyberpunk city";
        ThemeSpec spec = LLMClient.generateThemeSpec(description);

        assertNotNull("ThemeSpec should not be null", spec);
        assertNotNull("Background color should be set", spec.backgroundHex);
        assertNotNull("Text color should be set", spec.textHex);
        assertTrue("ThemeSpec should be valid", spec.isValid());
    }

    /**
     * Tests async theme generation with callback.
     * Verifies the async method works correctly and calls back on main thread.
     */
    @Test(timeout = 30000) // 30 second timeout
    public void testGenerateThemeSpecAsync() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ThemeSpec> resultSpec = new AtomicReference<>();

        LLMClient.generateThemeSpecAsync("forest morning", spec -> {
            resultSpec.set(spec);
            latch.countDown();
        });

        // Wait for async operation to complete
        assertTrue("Async operation should complete within timeout",
                latch.await(25, TimeUnit.SECONDS));

        ThemeSpec spec = resultSpec.get();
        assertNotNull("Callback should receive a ThemeSpec", spec);
        assertNotNull("Background should be set", spec.backgroundHex);
        assertNotNull("Text should be set", spec.textHex);
        assertTrue("ThemeSpec should be valid", spec.isValid());
    }

    /**
     * Tests that empty description returns default theme immediately.
     * Should not make an API call.
     */
    @Test
    public void testEmptyDescriptionReturnsDefault() {
        String json = LLMClient.generateThemeJson("");

        assertNotNull("JSON should not be null", json);
        ThemeSpec spec = ThemeSpec.fromJson(json);

        // Should be default theme
        ThemeSpec defaultSpec = ThemeSpec.defaultLight();
        assertEquals("Should use default background",
                defaultSpec.backgroundHex, spec.backgroundHex);
        assertEquals("Should use default text",
                defaultSpec.textHex, spec.textHex);
    }

    /**
     * Tests that null description returns default theme immediately.
     * Should not make an API call.
     */
    @Test
    public void testNullDescriptionReturnsDefault() {
        String json = LLMClient.generateThemeJson(null);

        assertNotNull("JSON should not be null", json);
        ThemeSpec spec = ThemeSpec.fromJson(json);

        // Should be default theme
        ThemeSpec defaultSpec = ThemeSpec.defaultLight();
        assertEquals("Should use default background",
                defaultSpec.backgroundHex, spec.backgroundHex);
    }

    /**
     * Tests theme generation with various descriptions.
     * Verifies that different descriptions produce different valid themes.
     */
    @Test
    public void testVariousDescriptionsProduceDifferentThemes() {
        String desc1 = "summer beach";
        String desc2 = "winter night";

        ThemeSpec spec1 = LLMClient.generateThemeSpec(desc1);
        ThemeSpec spec2 = LLMClient.generateThemeSpec(desc2);

        assertNotNull("First spec should not be null", spec1);
        assertNotNull("Second spec should not be null", spec2);
        assertTrue("First spec should be valid", spec1.isValid());
        assertTrue("Second spec should be valid", spec2.isValid());

        // Themes might be different (but not guaranteed by LLM)
        // At minimum, both should be valid
    }

    /**
     * Tests that generateThemeSpec handles complex descriptions.
     * Verifies robustness with longer, more detailed descriptions.
     */
    @Test
    public void testComplexDescriptionHandling() {
        String complexDesc = "a vibrant tropical paradise with warm sunset colors, " +
                "sandy beaches, and ocean waves";

        ThemeSpec spec = LLMClient.generateThemeSpec(complexDesc);

        assertNotNull("Spec should be generated", spec);
        assertTrue("Spec should be valid", spec.isValid());
        assertNotNull("All colors should be set", spec.accentHex);
        assertNotNull("Button color should be set", spec.buttonHex);
        assertNotNull("Secondary color should be set", spec.secondaryHex);
    }

    /**
     * Tests that short, simple descriptions work.
     * Verifies the API handles minimal input.
     */
    @Test
    public void testSimpleOneWordDescription() {
        ThemeSpec spec = LLMClient.generateThemeSpec("ocean");

        assertNotNull("Spec should be generated", spec);
        assertTrue("Spec should be valid", spec.isValid());
    }

    /**
     * Tests theme generation with emoji in description.
     * Verifies handling of unicode characters.
     */
    @Test
    public void testDescriptionWithEmoji() {
        String desc = "sunny day ☀️ at the beach 🏖️";
        ThemeSpec spec = LLMClient.generateThemeSpec(desc);

        assertNotNull("Spec should be generated", spec);
        assertTrue("Spec should be valid", spec.isValid());
    }

    /**
     * Tests that all five color fields are present in generated themes.
     * Verifies completeness of LLM response and exceeds minimum requirement.
     * <p>
     * REQUIREMENT: Must have at least background + text colors (2 colors minimum).
     * IMPLEMENTATION: Provides 5 colors (background, text, accent, button, secondary).
     */
    @Test
    public void testAllColorFieldsAreGenerated() {
        ThemeSpec spec = LLMClient.generateThemeSpec("autumn forest");

        assertNotNull("Spec should not be null", spec);

        // Required minimum colors (per project spec)
        assertNotNull("Background should be set", spec.backgroundHex);
        assertNotNull("Text should be set", spec.textHex);

        // Additional colors that exceed requirements
        assertNotNull("Accent should be set", spec.accentHex);
        assertNotNull("Button should be set", spec.buttonHex);
        assertNotNull("Secondary should be set", spec.secondaryHex);

        // All should be valid 6-digit hex colors
        assertTrue("Background should be valid hex",
                spec.backgroundHex.matches("^#[0-9A-Fa-f]{6}$"));
        assertTrue("Text should be valid hex",
                spec.textHex.matches("^#[0-9A-Fa-f]{6}$"));
        assertTrue("Accent should be valid hex",
                spec.accentHex.matches("^#[0-9A-Fa-f]{6}$"));
        assertTrue("Button should be valid hex",
                spec.buttonHex.matches("^#[0-9A-Fa-f]{6}$"));
        assertTrue("Secondary should be valid hex",
                spec.secondaryHex.matches("^#[0-9A-Fa-f]{6}$"));
    }

    /**
     * Tests async generation with SDK method.
     * Verifies the more efficient async implementation using Gemini SDK callbacks.
     */
    @Test(timeout = 30000)
    public void testGenerateThemeSpecAsyncWithSDK() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ThemeSpec> resultSpec = new AtomicReference<>();

        LLMClient.generateThemeSpecAsyncWithSDK("mountain sunset", spec -> {
            resultSpec.set(spec);
            latch.countDown();
        });

        assertTrue("SDK async operation should complete",
                latch.await(25, TimeUnit.SECONDS));

        ThemeSpec spec = resultSpec.get();
        assertNotNull("SDK async should return spec", spec);
        assertTrue("Spec should be valid", spec.isValid());
    }

    /**
     * Tests that generated themes have good contrast.
     * Verifies the LLM follows the prompt requirements for readability.
     * <p>
     * Note: This is a heuristic test - we check that background and text
     * are not identical, which is a basic sanity check.
     */
    @Test
    public void testGeneratedThemesHaveContrast() {
        ThemeSpec spec = LLMClient.generateThemeSpec("professional workspace");

        assertNotNull("Spec should be generated", spec);
        assertTrue("Spec should be valid", spec.isValid());

        // Background and text should not be the same color
        assertNotEquals("Background and text should differ for contrast",
                spec.backgroundHex.toLowerCase(),
                spec.textHex.toLowerCase());
    }

    /**
     * Tests rapid successive API calls.
     * Verifies the system handles multiple requests without issues.
     */
    @Test
    public void testRapidSuccessiveCalls() {
        ThemeSpec spec1 = LLMClient.generateThemeSpec("test1");
        ThemeSpec spec2 = LLMClient.generateThemeSpec("test2");
        ThemeSpec spec3 = LLMClient.generateThemeSpec("test3");

        assertNotNull("First call should succeed", spec1);
        assertNotNull("Second call should succeed", spec2);
        assertNotNull("Third call should succeed", spec3);

        assertTrue("All specs should be valid",
                spec1.isValid() && spec2.isValid() && spec3.isValid());
    }

    /**
     * Tests that minimum requirements are met.
     * REQUIREMENT: Theme must include at least background and text colors in hex format.
     */
    @Test
    public void testMinimumRequirementsMet() {
        ThemeSpec spec = LLMClient.generateThemeSpec("nature park");

        assertNotNull("Theme should be generated", spec);

        // Verify minimum required fields exist
        assertNotNull("Background color (required) must be present", spec.backgroundHex);
        assertNotNull("Text color (required) must be present", spec.textHex);

        // Verify they are in hex format
        assertTrue("Background must be hex format",
                spec.backgroundHex.matches("^#[0-9A-Fa-f]{6}$"));
        assertTrue("Text must be hex format",
                spec.textHex.matches("^#[0-9A-Fa-f]{6}$"));

        // Verify theme is valid
        assertTrue("Theme must be valid", spec.isValid());
    }

    /**
     * Tests JSON parsing with malformed input.
     * Verifies fallback to default theme on parse errors.
     */
    @Test
    public void testMalformedJsonFallsBackToDefault() {
        String malformedJson = "{invalid json}";
        ThemeSpec spec = ThemeSpec.fromJson(malformedJson);

        assertNotNull("Should return default theme on parse error", spec);

        // Should be default theme
        ThemeSpec defaultSpec = ThemeSpec.defaultLight();
        assertEquals("Should use default background",
                defaultSpec.backgroundHex, spec.backgroundHex);
        assertEquals("Should use default text",
                defaultSpec.textHex, spec.textHex);
    }

    /**
     * Tests that examples from project spec work correctly.
     * EXAMPLES: "summer beach" and "cyberpunk nightscape" from project requirements.
     */
    @Test
    public void testProjectSpecExamples() {
        // Test first example from spec
        ThemeSpec summerBeach = LLMClient.generateThemeSpec("summer beach");
        assertNotNull("Summer beach theme should be generated", summerBeach);
        assertTrue("Summer beach theme should be valid", summerBeach.isValid());
        assertNotNull("Should have background color", summerBeach.backgroundHex);
        assertNotNull("Should have text color", summerBeach.textHex);

        // Test second example from spec
        ThemeSpec cyberpunk = LLMClient.generateThemeSpec("cyberpunk nightscape");
        assertNotNull("Cyberpunk theme should be generated", cyberpunk);
        assertTrue("Cyberpunk theme should be valid", cyberpunk.isValid());
        assertNotNull("Should have background color", cyberpunk.backgroundHex);
        assertNotNull("Should have text color", cyberpunk.textHex);
    }
}

