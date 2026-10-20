package io.github.vexisvx.ledgerx.ui.component;

import io.github.vexisvx.ledgerx.config.AppConfig;

public class Icons {

    private static boolean useIcons() {
        return "icon".equals(AppConfig.getDisplayMode());
    }

    // Sidebar
    public static String dashboard()    { return useIcons() ? "\uD83D\uDCCA" : "[DB]"; }
    public static String transactions() { return useIcons() ? "\uD83D\uDCB3" : "[TX]"; }
    public static String categories()   { return useIcons() ? "\uD83D\uDCC1" : "[CA]"; }
    public static String budgets()      { return useIcons() ? "\uD83C\uDFAF" : "[BU]"; }
    public static String goals()        { return useIcons() ? "\uD83C\uDFC6" : "[GO]"; }
    public static String recurring()    { return useIcons() ? "\uD83D\uDD04" : "[RE]"; }
    public static String settings()     { return useIcons() ? "\u2699\uFE0F" : "[SE]"; }
    public static String about()        { return useIcons() ? "\u2139\uFE0F" : "[i]"; }

    // Actions (localized text passed in by caller)
    public static String edit(String localizedText) {
        return useIcons() ? "\u270F\uFE0F" : localizedText;
    }
    public static String delete(String localizedText) {
        return useIcons() ? "\uD83D\uDDD1\uFE0F" : localizedText;
    }
    public static String pause(String localizedText) {
        return useIcons() ? "\u23F8\uFE0F" : localizedText;
    }
    public static String play(String localizedText) {
        return useIcons() ? "\u25B6\uFE0F" : localizedText;
    }
    public static String add(String localizedText) {
        return useIcons() ? "\u2795" : localizedText;
    }

    // Category icons — only meaningful in "icon" mode
    public static String categoryIcon(String name) {
        if (!useIcons()) {
            // In text mode, return first 2 chars of name
            if (name == null || name.isEmpty()) return "??";
            return name.substring(0, Math.min(2, name.length())).toUpperCase();
        }
        if (name == null) return "\uD83D\uDCC1";
        String n = name.toLowerCase();
        if (n.contains("food") || n.contains("خوراک")) return "\uD83C\uDF54";
        if (n.contains("transport") || n.contains("حمل")) return "\uD83D\uDE97";
        if (n.contains("housing") || n.contains("مسکن")) return "\uD83C\uDFE0";
        if (n.contains("salary") || n.contains("حقوق")) return "\uD83D\uDCB0";
        if (n.contains("shop") || n.contains("خرید")) return "\uD83D\uDECD";
        if (n.contains("health") || n.contains("سلامت")) return "\uD83C\uDFE5";
        if (n.contains("bill") || n.contains("قبض")) return "\uD83D\uDCC4";
        if (n.contains("entertain") || n.contains("سرگرمی")) return "\uD83C\uDFAE";
        return "\uD83D\uDCC1";
    }

    public static final String[] CATEGORY_ICONS = {
        "\uD83C\uDF54", "\uD83D\uDE97", "\uD83C\uDFE0", "\uD83D\uDCB0", "\uD83D\uDECD",
        "\uD83C\uDFE5", "\uD83D\uDCC4", "\uD83C\uDFAE", "\uD83C\uDF93", "\uD83C\uDF0D",
        "\uD83D\uDCBB", "\uD83D\uDCF1", "\u2615", "\u2708", "\uD83C\uDFAC",
        "\uD83C\uDFB5", "\uD83D\uDC3E", "\uD83C\uDF3F", "\u2B50", "\uD83C\uDF81"
    };
}
