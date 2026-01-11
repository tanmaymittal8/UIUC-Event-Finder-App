package edu.uiuc.cs427app;

import org.json.JSONException;
import org.json.JSONObject;

public class ThemeSpec {
    // Required fields (minimum)
    public String backgroundHex = "#FFFFFF";
    public String textHex = "#111111";

    // Optional additional fields for more personalized themes
    public String accentHex = "#3D7DFF";
    public String buttonHex = "#1976D2";
    public String secondaryHex = "#F5F5F5";

    public String cardBackground;   // Panels / cards

    public String borderColor;      // Dividers / outlines

    public String headerColor;      // Toolbar / title color

    public String emoji;            // Decorative emoji, e.g., "🌲" or "🚀"

    /**
     * Creates a default light theme as fallback.
     */
    public static ThemeSpec defaultLight() {
        ThemeSpec t = new ThemeSpec();
        t.backgroundHex = "#FFFFFF";
        t.textHex = "#111111";
        t.accentHex = "#2E7D32"; // nice green accent
        t.buttonHex = "#1976D2"; // material blue
        t.secondaryHex = "#F5F5F5"; // light gray
        return t;
    }

    /**
     * Creates a ThemeSpec from a JSON string.
     * Falls back to default theme if parsing fails.
     *
     * @param json JSON string containing theme colors
     * @return ThemeSpec object
     */
    public static ThemeSpec fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return defaultLight();
        }

        try {
            JSONObject o = new JSONObject(json);
            ThemeSpec t = new ThemeSpec();

            // Use optString with defaults to avoid exceptions on missing fields
            // Required fields fall back to defaults if not present
            t.backgroundHex = o.optString("background", "#FFFFFF");
            t.textHex = o.optString("text", "#111111");
            t.accentHex = o.optString("accent", "#3D7DFF");
            t.buttonHex = o.optString("button", "#1976D2");
            t.secondaryHex = o.optString("secondary", "#F5F5F5");
            t.cardBackground = o.optString("cardBackground", null);
            t.borderColor = o.optString("borderColor", null);
            t.headerColor = o.optString("headerColor", null);
            t.emoji = o.optString("emoji", null);

            return t;
        } catch (JSONException e) {
            return defaultLight(); // safe fallback
        }
    }

    /**
     * Converts this ThemeSpec to a JSON string.
     *
     * @return JSON string representation
     */
    public String toJson() {
        try {
            JSONObject o = new JSONObject();
            o.put("background", backgroundHex);
            o.put("text", textHex);
            o.put("accent", accentHex);
            o.put("button", buttonHex);
            o.put("secondary", secondaryHex);
            o.put("cardBackground", cardBackground);
            o.put("borderColor", borderColor);
            o.put("headerColor", headerColor);
            o.put("emoji", emoji);
            return o.toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    /**
     * Validates that the theme has all required colors in proper hex format.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        // Only validate required fields; optional fields can be null
        return isValidHexColor(backgroundHex)
                && isValidHexColor(textHex)
                && isValidHexColor(accentHex)
                && isValidHexColor(buttonHex)
                && isValidHexColor(secondaryHex)
                && (cardBackground == null || isValidHexColor(cardBackground))
                && (borderColor == null || isValidHexColor(borderColor))
                && (headerColor == null || isValidHexColor(headerColor));
    }

    /**
     * Checks if a string is a valid hex color code.
     */
    private boolean isValidHexColor(String hex) {
        if (hex == null) return false;
        return hex.matches("^#[0-9A-Fa-f]{6}$");
    }
}
