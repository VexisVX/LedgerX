package io.github.vexisvx.ledgerx.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FormatterTest {

    @Test void persianDateConversion() {
        // 2026-01-01 → 1404/10/11
        int[] j = PersianDate.gregorianToJalali(2026, 1, 1);
        assertEquals(1404, j[0]);
        assertEquals(10, j[1]);
        assertEquals(11, j[2]);
    }

    @Test void persianDateString() {
        String s = PersianDate.toJalaliString(LocalDate.of(2026, 1, 1));
        assertEquals("1404/10/11", s);
    }

    @Test void monthNamePersian() {
        assertEquals("فروردین", PersianDate.monthName(1));
        assertEquals("اسفند", PersianDate.monthName(12));
    }
}
