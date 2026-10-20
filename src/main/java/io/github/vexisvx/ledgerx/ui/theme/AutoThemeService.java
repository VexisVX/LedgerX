package io.github.vexisvx.ledgerx.ui.theme;

import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.util.Locale;

public class AutoThemeService {

    /** Detects system dark mode. Returns true if dark. */
    public static boolean isSystemDark() {
        // Try Windows/macOS via Java AWT (works on some systems)
        try {
            // On Linux, use gsettings or env vars
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (os.contains("linux")) {
                // Check GTK theme
                Process p = new ProcessBuilder("gsettings", "get", "org.gnome.desktop.interface", "color-scheme")
                    .redirectErrorStream(true).start();
                String output = new String(p.getInputStream().readAllBytes()).trim().toLowerCase();
                if (output.contains("dark") || output.contains("prefer-dark")) return true;
                if (output.contains("light") || output.contains("prefer-light")) return false;

                // Fallback: check GTK_THEME env
                String gtk = System.getenv("GTK_THEME");
                if (gtk != null && gtk.toLowerCase().contains("dark")) return true;
            }
        } catch (Exception ignored) {}

        // Default: dark (we've been dark all along)
        return true;
    }

    /** Maps system mode to theme name. */
    public static String getAutoTheme(boolean dark) {
        return dark ? "teal" : "light-teal";
    }

    /** Returns the theme to apply: auto-detected or user's saved. */
    public static String resolveTheme() {
        String saved = io.github.vexisvx.ledgerx.config.AppConfig.getTheme();
        if ("auto".equals(saved)) {
            return getAutoTheme(isSystemDark());
        }
        return saved;
    }
}
