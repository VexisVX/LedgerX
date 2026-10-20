package io.github.vexisvx.ledgerx.service;

import io.github.vexisvx.ledgerx.dao.TransactionDao;
import io.github.vexisvx.ledgerx.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardService {

    private final TransactionDao txDao = new TransactionDao();
    private final BudgetService budgetService = new BudgetService();
    private final GoalService goalService = new GoalService();

    public BigDecimal getTotalIncome() { return txDao.sumByType(TransactionType.INCOME); }
    public BigDecimal getTotalExpense() { return txDao.sumByType(TransactionType.EXPENSE); }
    public BigDecimal getNet() { return getTotalIncome().subtract(getTotalExpense()); }
    public BigDecimal getMonthIncome(int y, int m) { return txDao.sumByTypeMonth(TransactionType.INCOME, y, m); }
    public BigDecimal getMonthExpense(int y, int m) { return txDao.sumByTypeMonth(TransactionType.EXPENSE, y, m); }

    public List<Map.Entry<String, BigDecimal>> getTopSpendingCategories(int y, int m, int limit) {
        List<Transaction> all = txDao.findAll();
        Map<String, BigDecimal> map = new HashMap<>();
        for (Transaction t : all) {
            if (t.getType() != TransactionType.EXPENSE) continue;
            if (t.getDate().getYear() != y || t.getDate().getMonthValue() != m) continue;
            String key = t.getCategoryName() != null ? t.getCategoryName() : "Uncategorized";
            map.merge(key, t.getAmount(), BigDecimal::add);
        }
        return map.entrySet().stream()
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    public BigDecimal getDailyAverageExpense() {
        LocalDate now = LocalDate.now();
        int day = now.getDayOfMonth();
        if (day == 0) return BigDecimal.ZERO;
        return getMonthExpense(now.getYear(), now.getMonthValue())
            .divide(BigDecimal.valueOf(day), 2, RoundingMode.HALF_UP);
    }

    public Transaction getBiggestExpenseThisMonth() {
        LocalDate now = LocalDate.now();
        return txDao.findAll().stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .filter(t -> t.getDate().getYear() == now.getYear()
                      && t.getDate().getMonthValue() == now.getMonthValue())
            .max(Comparator.comparing(Transaction::getAmount))
            .orElse(null);
    }

    public List<Budget> getCurrentBudgets() {
        LocalDate now = LocalDate.now();
        return budgetService.getAllWithSpending(now.getYear(), now.getMonthValue());
    }

    public List<Goal> getGoals() { return goalService.getAll(); }

    public long getOverbudgetCount() {
        return getCurrentBudgets().stream()
            .filter(b -> b.getSpent().compareTo(b.getMonthlyLimit()) > 0)
            .count();
    }

    /**
     * Health Score 0-100 based on:
     *  - Savings rate (40%): net / income this month
     *  - Budget usage (40%): how many budgets under limit
     *  - Activity (20%): having recent transactions
     */
    public int getHealthScore() {
        int score = 0;

        // Savings rate (40 pts)
        LocalDate now = LocalDate.now();
        BigDecimal mi = getMonthIncome(now.getYear(), now.getMonthValue());
        BigDecimal me = getMonthExpense(now.getYear(), now.getMonthValue());
        if (mi.compareTo(BigDecimal.ZERO) > 0) {
            double ratio = mi.subtract(me).doubleValue() / mi.doubleValue();
            if (ratio >= 0.3) score += 40;
            else if (ratio >= 0.1) score += 30;
            else if (ratio >= 0) score += 20;
            else score += 5;
        } else if (me.compareTo(BigDecimal.ZERO) == 0) {
            score += 20; // neutral, no data
        }

        // Budget usage (40 pts)
        List<Budget> budgets = getCurrentBudgets();
        if (!budgets.isEmpty()) {
            long under = budgets.stream()
                .filter(b -> b.getSpent().compareTo(b.getMonthlyLimit()) <= 0)
                .count();
            score += (int) (40.0 * under / budgets.size());
        } else {
            score += 20; // neutral
        }

        // Activity (20 pts)
        List<Transaction> all = txDao.findAll();
        LocalDate monthAgo = now.minusDays(30);
        long recent = all.stream()
            .filter(t -> !t.getDate().isBefore(monthAgo))
            .count();
        if (recent >= 10) score += 20;
        else if (recent >= 3) score += 15;
        else if (recent >= 1) score += 10;

        return Math.min(100, score);
    }

    public String getHealthLabel(int score) {
        if (score >= 80) return "excellent";
        if (score >= 60) return "good";
        if (score >= 40) return "fair";
        return "poor";
    }

    public List<Transaction> getAllTransactions() { return txDao.findAll(); }

    public boolean hasAnyData() {
        return !txDao.findAll().isEmpty();
    }

    // ═══════════════════════════════════════════════════════════
    // STAGE 3: Quick Stats
    // ═══════════════════════════════════════════════════════════

    /** Total days in current month (28-31). */
    public int getDaysInMonth() {
        return java.time.LocalDate.now().lengthOfMonth();
    }

    /** Elapsed days including today (always >= 1). */
    public int getElapsedDays() {
        return Math.max(1, java.time.LocalDate.now().getDayOfMonth());
    }

    /** Days left AFTER today in this month. Can be 0 on month-end. */
    public int getDaysLeftInMonth() {
        java.time.LocalDate now = java.time.LocalDate.now();
        return Math.max(0, now.lengthOfMonth() - now.getDayOfMonth());
    }

    /**
     * Projected month expense = actual expense / elapsed_days * total_days.
     * Uses BigDecimal with HALF_UP rounding to 2 decimals.
     * Returns ZERO if no expense data.
     */
    public BigDecimal getProjectedMonthExpense() {
        try {
            java.time.LocalDate now = java.time.LocalDate.now();
            BigDecimal actual = getMonthExpense(now.getYear(), now.getMonthValue());
            if (actual == null || actual.compareTo(BigDecimal.ZERO) <= 0) {
                return BigDecimal.ZERO;
            }
            int elapsed = getElapsedDays();
            int total = getDaysInMonth();
            return actual.divide(BigDecimal.valueOf(elapsed), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(total))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Projected savings = actual income - projected expense.
     * Returns 0 if income data missing.
     */
    public BigDecimal getProjectedSavings() {
        try {
            java.time.LocalDate now = java.time.LocalDate.now();
            BigDecimal income = getMonthIncome(now.getYear(), now.getMonthValue());
            if (income == null || income.compareTo(BigDecimal.ZERO) <= 0) {
                return BigDecimal.ZERO;
            }
            return income.subtract(getProjectedMonthExpense());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Previous month daily average (for comparison).
     * Returns null if no data in previous month.
     */
    public BigDecimal getPreviousMonthDailyAvg() {
        try {
            java.time.LocalDate prev = java.time.LocalDate.now().minusMonths(1);
            BigDecimal prevExp = getMonthExpense(prev.getYear(), prev.getMonthValue());
            if (prevExp == null || prevExp.compareTo(BigDecimal.ZERO) <= 0) return null;
            int prevDays = prev.lengthOfMonth();
            return prevExp.divide(BigDecimal.valueOf(prevDays), 2, java.math.RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }
}
