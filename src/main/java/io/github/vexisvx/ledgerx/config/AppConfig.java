package io.github.vexisvx.ledgerx.config;

import java.util.prefs.Preferences;

public class AppConfig {
    private static final Preferences prefs = Preferences.userNodeForPackage(AppConfig.class);

    public static final String APP_NAME = "LedgerX";
    public static final String APP_VERSION = "2.0.0";
    public static final String AUTHOR = "Younes Ahmadi";
    public static final String AUTHOR_FA = "\u06CC\u0648\u0646\u0633 \u0627\u062D\u0645\u062F\u06CC";
    public static final String BRAND = "VexisVX";
    public static final String GITHUB = "https://github.com/VexisVX";
    public static final String REPO = "https://github.com/VexisVX/LedgerX";

    public static String getLanguage() { return prefs.get("lang", "en"); }
    public static void setLanguage(String v) { prefs.put("lang", v); }

    public static String getTheme() { return prefs.get("theme", "auto"); }
    public static void setTheme(String v) { prefs.put("theme", v); }

    public static String getCurrency() { return prefs.get("currency", "USD"); }
    public static void setCurrency(String v) { prefs.put("currency", v); }

    public static String getDateMode() { return prefs.get("dateMode", "gregorian"); }
    public static void setDateMode(String v) { prefs.put("dateMode", v); }

    public static boolean isCompactMode() { return "true".equals(prefs.get("compactMode", "false")); }
    public static void setCompactMode(boolean v) { prefs.put("compactMode", String.valueOf(v)); }

    public static boolean isShowQuickAdd() { return "true".equals(prefs.get("showQuickAdd", "true")); }
    public static void setShowQuickAdd(boolean v) { prefs.put("showQuickAdd", String.valueOf(v)); }

    public static int getFabOffset() { return prefs.getInt("fabOffset", 100); }
    public static void setFabOffset(int v) { prefs.putInt("fabOffset", v); }

    public static String getPin() { return prefs.get("pin", ""); }
    public static void setPin(String v) { prefs.put("pin", v); }
    public static boolean isPinEnabled() { return !getPin().isEmpty(); }
}
