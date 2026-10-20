package io.github.vexisvx.ledgerx.ui.theme;

import javafx.scene.text.Font;

import java.util.Locale;

public class FontLoader {

    private static boolean loaded = false;
    private static String baseFamily = "System";

    public static void load() {
        if (loaded) return;
        loaded = true;

        // Try to load Vazirmatn from resources (if user added it)
        String[] candidates = {
            "/fonts/Vazirmatn-Regular.ttf",
            "/fonts/Vazirmatn-Medium.ttf"
        };
        for (String path : candidates) {
            try (var in = FontLoader.class.getResourceAsStream(path)) {
                if (in != null) {
                    Font f = Font.loadFont(in, 13);
                    if (f != null) {
                        baseFamily = f.getFamily();
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }

        // Fallback to system fonts if Vazirmatn is not available
        if ("System".equals(baseFamily)) {
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (os.contains("linux")) {
                baseFamily = "DejaVu Sans";
            } else if (os.contains("win")) {
                baseFamily = "Segoe UI";
            } else if (os.contains("mac")) {
                baseFamily = "SF Pro Text";
            }
        }
    }

    public static String getBaseFamily() {
        load();
        return baseFamily;
    }
}
