package io.github.vexisvx.ledgerx.util;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Validator {

    // ─── Amount ───
    public static void validateAmount(BigDecimal a) {
        if (a == null) throw new IllegalArgumentException("Amount is required");
        if (a.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0");
        if (a.compareTo(new BigDecimal("999999999999")) > 0)
            throw new IllegalArgumentException("Amount is too large");
    }

    // ─── Title ───
    public static void validateTitle(String t) {
        if (t == null || t.trim().isEmpty())
            throw new IllegalArgumentException("Title is required");
        if (t.trim().length() < 2)
            throw new IllegalArgumentException("Title must be at least 2 characters");
        if (t.trim().length() > 100)
            throw new IllegalArgumentException("Title is too long (max 100)");
    }

    // ─── Description ───
    public static void validateDescription(String d) {
        if (d != null && d.length() > 500)
            throw new IllegalArgumentException("Description is too long (max 500)");
    }

    // ─── Date (past or today only) — for transactions ───
    public static void validateDate(LocalDate d) {
        if (d == null) throw new IllegalArgumentException("Date is required");
        if (d.isAfter(LocalDate.now().plusDays(1)))
            throw new IllegalArgumentException("Date cannot be in the future");
        if (d.isBefore(LocalDate.of(1900, 1, 1)))
            throw new IllegalArgumentException("Date is too old");
    }

    // ─── Date (future only) — for recurring nextDate ───
    public static void validateFutureDate(LocalDate d) {
        if (d == null) throw new IllegalArgumentException("Date is required");
        if (d.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Next date cannot be in the past");
        if (d.isAfter(LocalDate.now().plusYears(50)))
            throw new IllegalArgumentException("Date is too far in the future");
    }

    // ─── Category / Goal name ───
    public static void validateName(String n) {
        if (n == null || n.trim().isEmpty())
            throw new IllegalArgumentException("Name is required");
        if (n.trim().length() < 2)
            throw new IllegalArgumentException("Name must be at least 2 characters");
        if (n.trim().length() > 50)
            throw new IllegalArgumentException("Name is too long (max 50)");
    }

    // ─── Budget ───
    public static void validateBudgetLimit(BigDecimal l) {
        if (l == null) throw new IllegalArgumentException("Budget is required");
        if (l.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Budget must be greater than 0");
        if (l.compareTo(new BigDecimal("999999999999")) > 0)
            throw new IllegalArgumentException("Budget is too large");
    }

    // ─── Goal ───
    public static void validateGoal(BigDecimal target, BigDecimal saved, LocalDate deadline) {
        if (target == null || target.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Target must be greater than 0");
        if (saved != null && saved.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Saved cannot be negative");
        if (saved != null && saved.compareTo(target) > 0)
            throw new IllegalArgumentException("Saved cannot exceed target");
        if (deadline == null)
            throw new IllegalArgumentException("Deadline is required");
        if (deadline.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Deadline cannot be in the past");
    }

    // ─── Parse helpers ───
    public static BigDecimal parseAmount(String s) {
        if (s == null || s.trim().isEmpty())
            throw new IllegalArgumentException("Amount is required");
        String cleaned = s.trim().replace(",", "").replace(" ", "");
        if (!cleaned.matches("\\d+(\\.\\d{1,2})?"))
            throw new IllegalArgumentException("Invalid amount format");
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format");
        }
    }
}
