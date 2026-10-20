package io.github.vexisvx.ledgerx.ui.theme;

import io.github.vexisvx.ledgerx.config.AppConfig;
import io.github.vexisvx.ledgerx.ui.i18n.I18n;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static final String BASE_CSS = "/css/base.css";
    private static final String[] THEMES = {
        "auto", "teal", "purple", "orange", "rose", "slate",
        "light-teal", "light-blue", "cream"
    };

    private static final List<Window> registeredWindows = new ArrayList<>();

    public static String currentThemePath() {
        String t = AutoThemeService.resolveTheme();
        return "/css/theme-" + t + ".css";
    }

    public static void registerWindow(Window w) {
        if (!registeredWindows.contains(w)) registeredWindows.add(w);
        w.setOnHidden(e -> registeredWindows.remove(w));
    }

    public static void applyAll() {
        for (Window w : new ArrayList<>(registeredWindows)) {
            if (w != null && w.getScene() != null) apply(w.getScene());
        }
    }

    public static void apply(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();
        // base.css first (defines default theme variables)
        var base = ThemeManager.class.getResource(BASE_CSS);
        if (base != null) scene.getStylesheets().add(base.toExternalForm());
        // theme overrides base variables
        var theme = ThemeManager.class.getResource(currentThemePath());
        if (theme != null) scene.getStylesheets().add(theme.toExternalForm());

        // RTL/LTR
        if (scene.getRoot() != null) {
            scene.getRoot().setNodeOrientation(
                I18n.isRtl() ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
        }
    }

    public static void setTheme(String themeName) {
        if (themeName == null) return;
        AppConfig.setTheme(themeName);
        ChartColorManager.resetRotation();
        applyAll();
    }

    public static void setTheme(Scene scene, String themeName) {
        if (themeName == null) return;
        AppConfig.setTheme(themeName);
        if (scene != null) apply(scene);
        applyAll();
    }

    public static boolean isValidTheme(String name) {
        for (String t : THEMES) if (t.equals(name)) return true;
        return false;
    }

    public static String[] allThemes() { return THEMES.clone(); }
}
