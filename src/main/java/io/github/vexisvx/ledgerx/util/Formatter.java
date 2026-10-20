package io.github.vexisvx.ledgerx.util;

import io.github.vexisvx.ledgerx.config.AppConfig;
import io.github.vexisvx.ledgerx.ui.i18n.I18n;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Formatter {
    private static final DecimalFormat NUM = new DecimalFormat("#,##0.00");

    public static String money(BigDecimal v) {
        if (v == null) v = BigDecimal.ZERO;
        String cur = AppConfig.getCurrency();
        String symbol;
        if ("IRT".equals(cur)) {
            symbol = I18n.t("currency.irt");
        } else if ("IRR".equals(cur)) {
            symbol = I18n.t("currency.irr");
        } else if ("EUR".equals(cur)) {
            symbol = "\u20AC";
        } else {
            symbol = "$";
        }
        boolean suffix = "IRT".equals(cur) || "IRR".equals(cur);
        return suffix ? (NUM.format(v) + " " + symbol) : (symbol + " " + NUM.format(v));
    }

    public static String date(LocalDate d) {
        if (d == null) return "-";
        if ("jalali".equals(AppConfig.getDateMode())) {
            return PersianDate.toJalaliString(d);
        }
        return d.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
