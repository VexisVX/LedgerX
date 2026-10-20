package io.github.vexisvx.ledgerx.util;

/**
 * Reshape Arabic/Persian letters to connected forms.
 * NO reversal — OpenPDF RTL handles display order.
 */
public class ArabicReshaper {

    private static final String[][] FORMS = {
        {"\u0621", null, null, null}, {"\u0622", "\uFE81", "\uFE81", null},
        {"\u0623", "\uFE83", "\uFE83", null}, {"\u0624", "\uFE85", "\uFE85", null},
        {"\u0625", "\uFE87", "\uFE87", null}, {"\u0626", "\uFE89", "\uFE8B", "\uFE8C"},
        {"\u0627", "\uFE8D", "\uFE8D", null}, {"\u0628", "\uFE8F", "\uFE91", "\uFE92"},
        {"\u0629", "\uFE93", "\uFE93", null}, {"\u062A", "\uFE95", "\uFE97", "\uFE98"},
        {"\u062B", "\uFE99", "\uFE9B", "\uFE9C"}, {"\u062C", "\uFE9D", "\uFE9F", "\uFEA0"},
        {"\u062D", "\uFEA1", "\uFEA3", "\uFEA4"}, {"\u062E", "\uFEA5", "\uFEA7", "\uFEA8"},
        {"\u062F", "\uFEA9", "\uFEA9", null}, {"\u0630", "\uFEAB", "\uFEAB", null},
        {"\u0631", "\uFEAD", "\uFEAD", null}, {"\u0632", "\uFEAF", "\uFEAF", null},
        {"\u0633", "\uFEB1", "\uFEB3", "\uFEB4"}, {"\u0634", "\uFEB5", "\uFEB7", "\uFEB8"},
        {"\u0635", "\uFEB9", "\uFEBB", "\uFEBC"}, {"\u0636", "\uFEBD", "\uFEBF", "\uFEC0"},
        {"\u0637", "\uFEC1", "\uFEC3", "\uFEC4"}, {"\u0638", "\uFEC5", "\uFEC7", "\uFEC8"},
        {"\u0639", "\uFEC9", "\uFECB", "\uFECC"}, {"\u063A", "\uFECD", "\uFECF", "\uFED0"},
        {"\u0640", "\u0640", "\u0640", "\u0640"}, {"\u0641", "\uFED1", "\uFED3", "\uFED4"},
        {"\u0642", "\uFED5", "\uFED7", "\uFED8"}, {"\u0643", "\uFED9", "\uFEDB", "\uFEDC"},
        {"\u0644", "\uFEDD", "\uFEDF", "\uFEE0"}, {"\u0645", "\uFEE1", "\uFEE3", "\uFEE4"},
        {"\u0646", "\uFEE5", "\uFEE7", "\uFEE8"}, {"\u0647", "\uFEE9", "\uFEEB", "\uFEEC"},
        {"\u0648", "\uFEED", "\uFEED", null}, {"\u0649", "\uFEEF", "\uFEEF", null},
        {"\u064A", "\uFEF1", "\uFEF3", "\uFEF4"},
        {"\u067E", "\uFB56", "\uFB58", "\uFB59"}, {"\u0686", "\uFB7A", "\uFB7C", "\uFB7D"},
        {"\u0698", "\uFB8A", "\uFB8A", null}, {"\u06A9", "\uFB8E", "\uFB90", "\uFB91"},
        {"\u06AF", "\uFB92", "\uFB94", "\uFB95"}, {"\u06CC", "\uFBFC", "\uFBFE", "\uFBFF"}
    };

    private static final String NON_JOINERS =
        "\u0621\u0622\u0623\u0624\u0625\u0627" +
        "\u062F\u0630\u0631\u0632\u0648\u0649\u0698";

    public static String reshape(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder out = new StringBuilder();
        int len = input.length();

        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            String[] forms = lookup(c);
            if (forms == null) { out.append(c); continue; }

            boolean prevJoinsToNext = false;
            if (i > 0) {
                char prevChar = input.charAt(i - 1);
                prevJoinsToNext = lookup(prevChar) != null && NON_JOINERS.indexOf(prevChar) < 0;
            }

            boolean nextJoinsToPrev = false;
            if (i + 1 < len) {
                nextJoinsToPrev = lookup(input.charAt(i + 1)) != null;
            }

            String chosen;
            if (prevJoinsToNext && nextJoinsToPrev && forms[3] != null) chosen = forms[3];
            else if (prevJoinsToNext && forms[1] != null) chosen = forms[1];
            else if (nextJoinsToPrev && forms[2] != null) chosen = forms[2];
            else chosen = forms[0];

            out.append(chosen);
        }
        return out.toString();
    }

    private static String[] lookup(char c) {
        for (String[] row : FORMS) {
            if (row[0].charAt(0) == c) return row;
        }
        return null;
    }
}
