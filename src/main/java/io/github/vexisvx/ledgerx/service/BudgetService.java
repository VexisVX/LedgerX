package io.github.vexisvx.ledgerx.service;

import io.github.vexisvx.ledgerx.dao.BudgetDao;
import io.github.vexisvx.ledgerx.dao.TransactionDao;
import io.github.vexisvx.ledgerx.model.Budget;
import io.github.vexisvx.ledgerx.model.Transaction;
import io.github.vexisvx.ledgerx.model.TransactionType;
import io.github.vexisvx.ledgerx.util.Validator;

import java.math.BigDecimal;
import java.util.List;

public class BudgetService {
    private final BudgetDao budgetDao = new BudgetDao();
    private final TransactionDao transactionDao = new TransactionDao();

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

    public void setBudget(long categoryId, BigDecimal limit) {
        Validator.validateBudgetLimit(limit);
        budgetDao.upsert(categoryId, limit);
    }
    public void delete(long id) { budgetDao.delete(id); }

    /**
     * Count of active budgets whose spending exceeds their limit
     * for the current month. Uses the same period as BudgetService.
     */
    public int getOverbudgetCount() {
        try {
            java.time.LocalDate now = java.time.LocalDate.now();
            var budgets = getAllWithSpending(now.getYear(), now.getMonthValue());
            return (int) budgets.stream()
                .filter(b -> b.getSpent().compareTo(b.getMonthlyLimit()) > 0)
                .count();
        } catch (Exception e) {
            return 0;
        }
    }
}
