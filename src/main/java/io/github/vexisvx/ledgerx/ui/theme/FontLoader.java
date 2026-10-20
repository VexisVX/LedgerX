package io.github.vexisvx.ledgerx.ui.theme;

import javafx.scene.text.Font;

import java.util.Locale;

public class FontLoader {

    private static boolean loaded = false;
    private static String baseFamily = "System";
    private static String emojiFamily = "System";

    public static void load() {
        if (loaded) return;
        loaded = true;

        // ── Base font (Vazirmatn if available, else system) ──
        String[] baseCandidates = {
            "/fonts/Vazirmatn-Regular.ttf",
            "/fonts/Vazirmatn-Medium.ttf"
        };
        for (String path : baseCandidates) {
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

        if ("System".equals(baseFamily)) {
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (os.contains("linux")) baseFamily = "DejaVu Sans";
            else if (os.contains("win")) baseFamily = "Segoe UI";
            else if (os.contains("mac")) baseFamily = "SF Pro Text";
        }

        // ── Emoji font from resource (guaranteed) ──
        String[] emojiCandidates = {
            "/fonts/NotoEmoji-Regular.ttf",
            "/fonts/NotoColorEmoji.ttf"
        };
        for (String path : emojiCandidates) {
            try (var in = FontLoader.class.getResourceAsStream(path)) {
                if (in != null) {
                    // Load at size 14 — this registers the font family
                    Font f = Font.loadFont(in, 14);
                    if (f != null) {
                        emojiFamily = f.getFamily();
                        System.out.println("✓ Loaded emoji font: " + emojiFamily);
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not load emoji font from " + path + ": " + e.getMessage());
            }
        }

        if ("System".equals(emojiFamily)) {
            // Fallback to system emoji fonts
            String[] systemEmoji = {"Noto Color Emoji", "Segoe UI Emoji", "Apple Color Emoji", "Noto Emoji"};
            for (String name : systemEmoji) {
                if (Font.getFamilies().contains(name)) {
                    emojiFamily = name;
                    System.out.println("✓ Using system emoji font: " + name);
                    break;
                }
            }
        }

        System.out.println("Base font: " + baseFamily + ", Emoji font: " + emojiFamily);
    }

    public static String getBaseFamily() {
        load();
        return baseFamily;
    }

    public static String getEmojiFamily() {
        load();
        return emojiFamily;
    }
}
