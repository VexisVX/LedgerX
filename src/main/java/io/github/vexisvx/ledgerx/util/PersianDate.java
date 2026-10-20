package io.github.vexisvx.ledgerx.util;

import java.time.LocalDate;

public class PersianDate {
    private static final String[] J_MONTHS = {
        "فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور",
        "مهر","آبان","آذر","دی","بهمن","اسفند"
    };

    public static String toJalaliString(LocalDate g) {
        int gy = g.getYear(), gm = g.getMonthValue(), gd = g.getDayOfMonth();
        int[] j = gregorianToJalali(gy, gm, gd);
        return String.format("%04d/%02d/%02d", j[0], j[1], j[2]);
    }

    public static int[] gregorianToJalali(int gy, int gm, int gd) {
        int[] g_d_m = {0,31,59,90,120,151,181,212,243,273,304,334};
        int gy2 = (gm > 2) ? (gy + 1) : gy;
        long days = 355666 + (365L * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100)
                + ((gy2 + 399) / 400) + gd + g_d_m[gm - 1];
        int jy = -1595 + (33 * (int)(days / 12053));
        days %= 12053;
        jy += 4 * (int)(days / 1461);
        days %= 1461;
        if (days > 365) { jy += (days - 1) / 365; days = (days - 1) % 365; }
        int jm, jd;
        if (days < 186) { jm = 1 + (int)(days / 31); jd = 1 + (int)(days % 31); }
        else { jm = 7 + (int)((days - 186) / 30); jd = 1 + (int)((days - 186) % 30); }
        return new int[]{jy, jm, jd};
    }

    public static String monthName(int m) {
        if (m >= 1 && m <= 12) return J_MONTHS[m - 1];
        return "";
    }
}
