package io.github.vexisvx.ledgerx.service;

import io.github.vexisvx.ledgerx.dao.BudgetDao;
import io.github.vexisvx.ledgerx.dao.TransactionDao;
import io.github.vexisvx.ledgerx.model.Budget;
import io.github.vexisvx.ledgerx.model.Transaction;
import io.github.vexisvx.ledgerx.model.TransactionType;
import io.github.vexisvx.ledgerx.util.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class BudgetService {
    private final BudgetDao budgetDao = new BudgetDao();
    private final TransactionDao transactionDao = new TransactionDao();

    // ─── Forecast ───
    public static class Forecast {
        public final Budget budget;
        public final BigDecimal projectedSpend;
        public final BigDecimal overshoot;
        public final BigDecimal dailyAverage;
        public final int daysLeft;
        public final int daysInMonth;
        public final boolean willOverspend;
        public final boolean isAtRisk;

        public Forecast(Budget budget, BigDecimal projectedSpend, BigDecimal overshoot,
                        BigDecimal dailyAverage, int daysLeft, int daysInMonth) {
            this.budget = budget;
            this.projectedSpend = projectedSpend;
            this.overshoot = overshoot;
            this.dailyAverage = dailyAverage;
            this.daysLeft = daysLeft;
            this.daysInMonth = daysInMonth;
            this.willOverspend = overshoot.compareTo(BigDecimal.ZERO) > 0;
            this.isAtRisk = !willOverspend && budget.getUsagePercent() >= 75;
        }
    }

    public List<Budget> getAllWithSpending(int year, int month) {
        List<Budget> budgets = budgetDao.findAll();
        List<Transaction> all = transactionDao.findAll();
        for (Budget b : budgets) {
            BigDecimal spent = all.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getCategoryId() != null && t.getCategoryId().equals(b.getCategoryId()))
                .filter(t -> t.getDate().getYear() == year && t.getDate().getMonthValue() == month)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            b.setSpent(spent);
        }
        return budgets;
    }

    /** Forecasts end-of-month spending for each budget. */
    public List<Forecast> forecastEndOfMonth() {
        LocalDate today = LocalDate.now();
        YearMonth ym = YearMonth.from(today);
        int daysInMonth = ym.lengthOfMonth();
        int daysPassed = today.getDayOfMonth();
        int daysLeft = daysInMonth - daysPassed;

        List<Budget> budgets = getAllWithSpending(ym.getYear(), ym.getMonthValue());
        List<Forecast> out = new ArrayList<>();

        for (Budget b : budgets) {
            if (b.getMonthlyLimit() == null || b.getMonthlyLimit().compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal spent = b.getSpent();
            BigDecimal dailyAvg = daysPassed > 0
                ? spent.divide(BigDecimal.valueOf(daysPassed), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            BigDecimal projected = dailyAvg.multiply(BigDecimal.valueOf(daysInMonth));
            BigDecimal overshoot = projected.subtract(b.getMonthlyLimit());

            out.add(new Forecast(b, projected, overshoot, dailyAvg, daysLeft, daysInMonth));
        }
        return out;
    }

    /** Returns only the ones at risk or over budget. */
    public List<Forecast> getAlerts() {
        List<Forecast> alerts = new ArrayList<>();
        for (Forecast f : forecastEndOfMonth()) {
            if (f.willOverspend || f.isAtRisk) alerts.add(f);
        }
        return alerts;
    }

    public void setBudget(long categoryId, BigDecimal limit) {
        Validator.validateBudgetLimit(limit);
        budgetDao.upsert(categoryId, limit);
    }

    public void delete(long id) {
        budgetDao.delete(id);
    }
}
