package io.github.vexisvx.ledgerx.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    @Test void amountPositive() {
        assertDoesNotThrow(() -> Validator.validateAmount(new BigDecimal("100.50")));
    }

    @Test void amountZeroThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> Validator.validateAmount(BigDecimal.ZERO));
    }

    @Test void amountNullThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> Validator.validateAmount(null));
    }

    @Test void titleEmptyThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> Validator.validateTitle("  "));
    }

    @Test void titleValid() {
        assertDoesNotThrow(() -> Validator.validateTitle("Buy groceries"));
    }

    @Test void dateNullThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> Validator.validateDate(null));
    }

    @Test void dateTodayOk() {
        assertDoesNotThrow(() -> Validator.validateDate(LocalDate.now()));
    }

    @Test void dateFutureThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> Validator.validateDate(LocalDate.now().plusYears(1)));
    }

    @Test void budgetLimitPositive() {
        assertDoesNotThrow(() -> Validator.validateBudgetLimit(new BigDecimal("500000")));
    }

    @Test void budgetLimitZeroThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> Validator.validateBudgetLimit(BigDecimal.ZERO));
    }

    // ─── Goal: امضای جدید (target, saved, deadline) ───
    @Test void goalValid() {
        assertDoesNotThrow(() ->
            Validator.validateGoal(
                new BigDecimal("1000"),
                new BigDecimal("100"),
                LocalDate.now().plusMonths(3)));
    }

    @Test void goalInvalidTarget() {
        assertThrows(IllegalArgumentException.class,
            () -> Validator.validateGoal(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                LocalDate.now().plusMonths(1)));
    }

    @Test void goalSavedExceedsTarget() {
        assertThrows(IllegalArgumentException.class,
            () -> Validator.validateGoal(
                new BigDecimal("1000"),
                new BigDecimal("2000"),
                LocalDate.now().plusMonths(1)));
    }

    @Test void goalPastDeadline() {
        assertThrows(IllegalArgumentException.class,
            () -> Validator.validateGoal(
                new BigDecimal("1000"),
                BigDecimal.ZERO,
                LocalDate.now().minusDays(1)));
    }
}
