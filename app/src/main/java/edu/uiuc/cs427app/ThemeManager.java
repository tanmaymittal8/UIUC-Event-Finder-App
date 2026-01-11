package edu.uiuc.cs427app;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

public class ThemeManager {
    private static final String PREFS = "user_theme_prefs";
    private static final String KEY_SUFFIX = "_theme_json";


    /**
     * Loads a theme for a specific user.
     *
     * @param ctx      Context
     * @param username Username to load theme for
     * @return ThemeSpec (defaults to light theme if none exists)
     */
    public static ThemeSpec loadForUser(Context ctx, String username) {
        // Try DB first
        ThemeSpec fromDb = AuthenticationManager.getInstance(ctx).loadThemeSpecForUser(username);
        if (fromDb != null) return fromDb;

        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String json = sp.getString(username + "_theme_json", null);
        if (json != null && !json.isEmpty()) {
            try {
                return ThemeSpec.fromJson(json);
            } catch (Exception ignored) {
            }
        }
        return ThemeSpec.defaultLight();
    }

    /**
     * Applies a theme to an activity's UI.
     *
     * @param activity Activity to apply theme to
     * @param spec     ThemeSpec to apply
     */
    public static void apply(Activity activity, ThemeSpec spec) {
        View root = activity.findViewById(android.R.id.content);
        if (root instanceof ViewGroup) {
            applyRecursive((ViewGroup) root, spec);
        } else if (root != null) {
            root.setBackgroundColor(parseColorSafe(spec.backgroundHex));
        }

        applyToolbarAndEmoji(activity, spec);
    }

    /**
     * Recursively applies theme to all views in a view hierarchy.
     */
    private static void applyRecursive(ViewGroup group, ThemeSpec spec) {
        group.setBackgroundColor(parseColorSafe(spec.backgroundHex));

        for (int i = 0; i < group.getChildCount(); i++) {
            View v = group.getChildAt(i);

            if (v instanceof TextView && !(v instanceof Button)) {
                ((TextView) v).setTextColor(parseColorSafe(spec.textHex));
            }

            if (v instanceof Button b) {
                b.setTextColor(colorOr(spec.textHex, "#111111"));

                if (v.getClass().getName().equals("com.google.android.material.button.MaterialButton")) {
                    try {
                        b.getBackground().mutate();
                        androidx.core.view.ViewCompat.setBackgroundTintList(
                                b, android.content.res.ColorStateList.valueOf(colorOr(spec.buttonHex, "#1976D2"))
                        );
                    } catch (Throwable ignored) {
                        b.getBackground().mutate();
                        androidx.core.graphics.drawable.DrawableCompat.setTint(
                                b.getBackground(), colorOr(spec.buttonHex, "#1976D2")
                        );
                    }
                } else {
                    if (b.getBackground() != null) {
                        b.getBackground().mutate();
                        androidx.core.view.ViewCompat.setBackgroundTintList(
                                b, android.content.res.ColorStateList.valueOf(colorOr(spec.buttonHex, "#1976D2"))
                        );
                    }
                }
            }

            if (v instanceof androidx.cardview.widget.CardView) {
                ((CardView) v).setCardBackgroundColor(colorOr(
                        (spec.cardBackground != null && !spec.cardBackground.isEmpty()) ? spec.cardBackground : spec.secondaryHex,
                        "#F5F5F5"
                ));
            }

            Object tag = v.getTag();
            if (tag instanceof String t) {
                if ("border".equals(t)) {
                    v.setBackgroundColor(colorOr(spec.borderColor, "#DDDDDD"));
                } else if ("accent".equals(t)) {
                    v.setBackgroundColor(colorOr(spec.accentHex, "#3D7DFF"));
                }
            }

            if (v instanceof ViewGroup) {
                applyRecursive((ViewGroup) v, spec);
            }
        }
    }

    /**
     * Safely parses a hex color string.
     * Returns black as fallback if parsing fails.
     */
    private static int parseColorSafe(String hex) {
        try {
            return Color.parseColor(hex);
        } catch (Exception e) {
            return Color.BLACK;
        }
    }

    /**
     * Parses a hex color with fallback.
     *
     * @param hex         Primary hex color string
     * @param fallbackHex Fallback hex color string
     * @return Parsed color integer
     */
    private static int colorOr(String hex, String fallbackHex) {
        try {
            if (hex != null && !hex.isEmpty()) return Color.parseColor(hex);
        } catch (Exception ignored) {
        }
        return Color.parseColor(fallbackHex);
    }

    /**
     * Saves a theme specification for a user to database and SharedPreferences.
     *
     * @param ctx      Context
     * @param username Username to save theme for
     * @param spec     ThemeSpec to save
     */
    public static void saveForUser(Context ctx, String username, ThemeSpec spec) {
        AuthenticationManager.getInstance(ctx).saveThemeSpecForUser(username, spec);
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(username + "_theme_json", spec.toJson()).apply();
    }

    /**
     * Applies theme colors to toolbar/action bar and adds emoji if specified.
     *
     * @param activity Activity to apply toolbar theme to
     * @param spec     ThemeSpec containing toolbar colors and emoji
     */
    private static void applyToolbarAndEmoji(Activity activity, ThemeSpec spec) {
        final int header = colorOr(spec.headerColor, "#3D7DFF");
        final int titleColor = colorOr(spec.textHex, "#FFFFFF");
        final String emoji = (spec.emoji == null ? "" : spec.emoji);

        // 1) Try common toolbar IDs (Material & AppCompat projects vary)
        int tbId = 0;
        String[] candidates = new String[]{"toolbar", "topAppBar", "materialToolbar"};
        for (String name : candidates) {
            int id = activity.getResources().getIdentifier(name, "id", activity.getPackageName());
            if (id != 0) {
                tbId = id;
                break;
            }
        }

        if (tbId != 0) {
            View tb = activity.findViewById(tbId);
            if (tb != null) {
                String cls = tb.getClass().getName();

                if ("com.google.android.material.appbar.MaterialToolbar".equals(cls)) {
                    try {
                        ((androidx.appcompat.widget.Toolbar) tb).setTitleTextColor(titleColor);
                    } catch (Throwable ignored) { /* title set below if needed */ }
                    try {
                        androidx.core.view.ViewCompat.setBackgroundTintList(
                                tb, android.content.res.ColorStateList.valueOf(header));
                    } catch (Throwable ignored) {
                        tb.setBackgroundColor(header);
                    }

                    if (tb instanceof androidx.appcompat.widget.Toolbar mtb) {
                        CharSequence cur = mtb.getTitle();
                        String title = cur == null ? "" : cur.toString();
                        if (!emoji.isEmpty() && !title.contains(emoji)) {
                            mtb.setTitle((title.isEmpty() ? "" : title + " ") + emoji);
                        }
                    }
                    tintAppBarFamily(activity, header, titleColor, emoji);
                    return;
                }

                if (tb instanceof androidx.appcompat.widget.Toolbar toolbar) {
                    toolbar.setBackgroundColor(header);
                    toolbar.setTitleTextColor(titleColor);
                    CharSequence cur = toolbar.getTitle();
                    String title = cur == null ? "" : cur.toString();
                    if (!emoji.isEmpty() && !title.contains(emoji)) {
                        toolbar.setTitle((title.isEmpty() ? "" : title + " ") + emoji);
                    }
                    tintAppBarFamily(activity, header, titleColor, emoji);
                    return;
                }
            }
        }

        if (activity instanceof androidx.appcompat.app.AppCompatActivity) {
            androidx.appcompat.app.ActionBar ab =
                    ((androidx.appcompat.app.AppCompatActivity) activity).getSupportActionBar();
            if (ab != null) {
                try {
                    ab.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(header));
                } catch (Throwable ignored) {
                }
                if (!emoji.isEmpty()) {
                    CharSequence cur = ab.getTitle();
                    String title = cur == null ? "" : cur.toString();
                    if (!title.contains(emoji)) {
                        ab.setTitle((title.isEmpty() ? "" : title + " ") + emoji);
                    }
                }
                return;
            }
        }

        if (!emoji.isEmpty()) {
            CharSequence cur = activity.getTitle();
            String title = cur == null ? "" : cur.toString();
            if (!title.contains(emoji)) {
                activity.setTitle((title.isEmpty() ? "" : title + " ") + emoji);
            }
        }
    }

    /**
     * Tints AppBarLayout / CollapsingToolbarLayout if present (Material containers that can mask toolbar color).
     */
    private static void tintAppBarFamily(Activity activity, int headerColor, int titleColor, String emoji) {
        int appBarId = activity.getResources().getIdentifier("appbar", "id", activity.getPackageName());
        if (appBarId == 0)
            appBarId = activity.getResources().getIdentifier("appBar", "id", activity.getPackageName());
        if (appBarId == 0)
            appBarId = activity.getResources().getIdentifier("app_bar", "id", activity.getPackageName());

        if (appBarId != 0) {
            View appBar = activity.findViewById(appBarId);
            if (appBar != null) {
                try {
                    androidx.core.view.ViewCompat.setBackgroundTintList(
                            appBar, android.content.res.ColorStateList.valueOf(headerColor));
                } catch (Throwable ignored) {
                    appBar.setBackgroundColor(headerColor);
                }
            }
        }

        int ctlId = activity.getResources().getIdentifier("collapsing_toolbar", "id", activity.getPackageName());
        if (ctlId == 0)
            ctlId = activity.getResources().getIdentifier("collapsingToolbar", "id", activity.getPackageName());
        View ctlView = (ctlId != 0) ? activity.findViewById(ctlId) : null;

        if (ctlView != null && "com.google.android.material.appbar.CollapsingToolbarLayout".equals(ctlView.getClass().getName())) {
            try {
                com.google.android.material.appbar.CollapsingToolbarLayout ctl =
                        (com.google.android.material.appbar.CollapsingToolbarLayout) ctlView;
                ctl.setContentScrimColor(headerColor);
                ctl.setCollapsedTitleTextColor(titleColor);
                ctl.setExpandedTitleColor(titleColor);
                if (!emoji.isEmpty()) {
                    CharSequence cur = ctl.getTitle();
                    String title = cur == null ? "" : cur.toString();
                    if (!title.contains(emoji)) {
                        ctl.setTitle((title.isEmpty() ? "" : title + " ") + emoji);
                    }
                }
            } catch (Throwable ignored) { /* Material dependency may be absent */ }
        }
    }
}

