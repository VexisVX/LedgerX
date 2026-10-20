package io.github.vexisvx.ledgerx.ui.i18n;

import io.github.vexisvx.ledgerx.config.AppConfig;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    private static Locale locale;
    private static ResourceBundle bundle;

    static { reload(); }

    public static void reload() {
        String lang = AppConfig.getLanguage();
        locale = "en".equals(lang) ? Locale.ENGLISH : Locale.forLanguageTag("fa");
        bundle = ResourceBundle.getBundle("i18n.messages", locale);
    }

    public static void setLanguage(String lang) {
        AppConfig.setLanguage(lang);
        reload();
    }

    public static ResourceBundle getBundle() { return bundle; }

    public static String t(String key) {
        try { return bundle.getString(key); }
        catch (Exception e) { return "!" + key + "!"; }
    }

    public static String t(String key, Object... args) {
        try { return String.format(bundle.getString(key), args); }
        catch (Exception e) { return "!" + key + "!"; }
    }

    /** Returns null if key doesn't exist (useful for optional translations). */
    public static String tOrNull(String key) {
        try { return bundle.getString(key); }
        catch (Exception e) { return null; }
    }

    public static boolean isRtl() { return "fa".equals(locale.getLanguage()); }
    public static Locale getLocale() { return locale; }
}
