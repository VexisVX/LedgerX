package io.github.vexisvx.ledgerx.ui.theme;

import io.github.vexisvx.ledgerx.config.AppConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages chart color palettes based on the active theme.
 * Supports rotation for visual variety on each refresh.
 */
public class ChartColorManager {

    // ─── Palettes per theme (5 colors each) ───
    private static final Map<String, String[]> PALETTES = new HashMap<>();

    static {
        // teal: turquoise + blue theme
        PALETTES.put("teal", new String[]{
            "#f59e0b", "#3b82f6", "#a855f7", "#00d4aa", "#ec4899"
        });
        // purple: violet + cyan theme
        PALETTES.put("purple", new String[]{
            "#a855f7", "#ec4899", "#06b6d4", "#10b981", "#f59e0b"
        });
        // orange: warm earth theme
        PALETTES.put("orange", new String[]{
            "#ff8c42", "#ef4444", "#eab308", "#ec4899", "#a855f7"
        });
        // rose: pink + purple theme
        PALETTES.put("rose", new String[]{
            "#f43f8e", "#ef4444", "#a855f7", "#f59e0b", "#eab308"
        });
        // slate: blue-gray theme
        PALETTES.put("slate", new String[]{
            "#5b9dff", "#06b6d4", "#a855f7", "#10b981", "#f59e0b"
        });
        // light-teal
        PALETTES.put("light-teal", new String[]{
            "#00a884", "#0092ff", "#a855f7", "#f59e0b", "#ec4899"
        });
        // light-blue
        PALETTES.put("light-blue", new String[]{
            "#2563eb", "#06b6d4", "#7c3aed", "#ec4899", "#f59e0b"
        });
        // cream: warm light
        PALETTES.put("cream", new String[]{
            "#e07a1f", "#d4a017", "#a855f7", "#06b6d4", "#ef4444"
        });
        // auto: falls back to teal
        PALETTES.put("auto", PALETTES.get("teal"));
    }

    // ─── Rotation offset per refresh (persists across refresh) ───
    private static int rotationOffset = 0;

    /** Get palette for current theme, with current rotation offset applied. */
    public static String[] getPieColors() {
        String theme = AppConfig.getTheme();
        if ("auto".equals(theme)) theme = "teal";
        String[] base = PALETTES.getOrDefault(theme, PALETTES.get("teal"));
        return rotate(base, rotationOffset);
    }

    /** Income color (green-ish, theme-agnostic but theme-tinted). */
    public static String getIncomeColor() {
        String theme = AppConfig.getTheme();
        if ("auto".equals(theme)) theme = "teal";
        return switch (theme) {
            case "purple" -> "#a855f7";  // purple income
            case "orange" -> "#eab308";  // yellow income
            case "rose" -> "#f43f8e";    // pink income
            case "light-blue" -> "#3b82f6";
            case "cream" -> "#d4a017";
            default -> "#00d4aa";        // teal income
        };
    }

    /** Expense color (red-ish, theme-agnostic but theme-tinted). */
    public static String getExpenseColor() {
        String theme = AppConfig.getTheme();
        if ("auto".equals(theme)) theme = "teal";
        return switch (theme) {
            case "purple" -> "#f43f5e";
            case "orange" -> "#ef4444";
            case "rose" -> "#ef4444";
            case "light-blue" -> "#dc2626";
            case "cream" -> "#d94a3d";
            default -> "#ff6b6b";        // teal expense
        };
    }

    /** Advance rotation offset (called on each refresh). */
    public static void advanceRotation() {
        rotationOffset = (rotationOffset + 1) % 5;
    }

    /** Reset rotation (e.g., on theme change). */
    public static void resetRotation() {
        rotationOffset = 0;
    }

    private static String[] rotate(String[] arr, int offset) {
        if (arr == null || arr.length == 0) return new String[]{"#00d4aa"};
        int n = arr.length;
        String[] result = new String[n];
        for (int i = 0; i < n; i++) {
            result[i] = arr[(i + offset) % n];
        }
        return result;
    }
}
