package io.github.vexisvx.ledgerx.ui.component;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Colored circle + letter icons. No emoji, no font dependency.
 * Works on every OS and JavaFX version.
 */
public class Icons {

    /** Creates a colored circle with a letter inside. */
    public static StackPane badge(String letter, String colorHex, double size) {
        Circle circle = new Circle(size / 2);
        circle.setFill(Color.web(colorHex, 0.20));
        circle.setStroke(Color.web(colorHex, 0.60));
        circle.setStrokeWidth(1.2);

        Text text = new Text(letter);
        text.setFill(Color.web(colorHex));
        text.setFont(Font.font("Vazirmatn", FontWeight.BOLD, size * 0.42));

        StackPane pane = new StackPane(circle, text);
        pane.setPrefSize(size, size);
        pane.setMinSize(size, size);
        pane.setMaxSize(size, size);
        return pane;
    }

    /** Creates a filled circle (solid). */
    public static Circle dot(String colorHex, double size) {
        Circle c = new Circle(size / 2);
        c.setFill(Color.web(colorHex));
        return c;
    }

    // ─── Sidebar colors and letters ───

    public static String dashboardColor()    { return "#00d4aa"; }
    public static String dashboardLetter()   { return "D"; }

    public static String transactionsColor() { return "#3b82f6"; }
    public static String transactionsLetter(){ return "T"; }

    public static String categoriesColor()   { return "#a855f7"; }
    public static String categoriesLetter()  { return "C"; }

    public static String budgetsColor()      { return "#f59e0b"; }
    public static String budgetsLetter()     { return "B"; }

    public static String goalsColor()        { return "#10b981"; }
    public static String goalsLetter()       { return "G"; }

    public static String recurringColor()    { return "#06b6d4"; }
    public static String recurringLetter()   { return "R"; }

    public static String settingsColor()     { return "#64748b"; }
    public static String settingsLetter()    { return "S"; }

    public static String aboutColor()        { return "#8b5cf6"; }
    public static String aboutLetter()       { return "i"; }

    // ─── Category colors ───

    public static String categoryColor(String name) {
        if (name == null) return "#6a7a8a";
        String n = name.toLowerCase();
        if (n.contains("food") || n.contains("خوراک")) return "#f59e0b";
        if (n.contains("transport") || n.contains("حمل")) return "#3b82f6";
        if (n.contains("housing") || n.contains("مسکن")) return "#a855f7";
        if (n.contains("salary") || n.contains("حقوق")) return "#10b981";
        if (n.contains("shop") || n.contains("خرید")) return "#ec4899";
        if (n.contains("health") || n.contains("سلامت")) return "#ef4444";
        if (n.contains("bill") || n.contains("قبض")) return "#eab308";
        if (n.contains("entertain") || n.contains("سرگرمی")) return "#06b6d4";
        String[] colors = {"#f59e0b", "#3b82f6", "#a855f7", "#10b981", "#ec4899", "#ef4444", "#eab308", "#06b6d4"};
        return colors[Math.abs(name.hashCode()) % colors.length];
    }

    public static String categoryLetter(String name) {
        if (name == null || name.isBlank()) return "?";
        return name.substring(0, 1).toUpperCase();
    }

    public static String categoryShort(String name) {
        if (name == null || name.isBlank()) return "??";
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    /** Category color options for the icon picker. */
    public static final String[] CATEGORY_COLORS = {
        "#f59e0b", // Food / amber
        "#3b82f6", // Transport / blue
        "#a855f7", // Housing / purple
        "#10b981", // Salary / green
        "#ec4899", // Shopping / pink
        "#ef4444", // Health / red
        "#eab308", // Bills / yellow
        "#06b6d4", // Entertain / cyan
        "#8b5cf6", // Education / violet
        "#14b8a6", // Travel / teal
        "#f97316", // Tech / orange
        "#6366f1", // Mobile / indigo
        "#84cc16", // Coffee / lime
        "#0ea5e9", // Flight / sky
        "#d946ef", // Movie / fuchsia
        "#22c55e", // Music / green-500
        "#a3a3a3", // Pets / gray
        "#16a34a", // Nature / green-600
        "#eab308", // Star / yellow
        "#f43f5e"  // Gift / rose
    };

    /** Letters shown in category icon picker. */
    public static final String[] CATEGORY_LETTERS = {
        "F", "T", "H", "S", "S", "H", "B", "E", "E", "T",
        "T", "M", "C", "F", "M", "M", "P", "N", "★", "G"
    };
}
