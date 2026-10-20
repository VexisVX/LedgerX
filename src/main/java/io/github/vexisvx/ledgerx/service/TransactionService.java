package io.github.vexisvx.ledgerx.service;

import io.github.vexisvx.ledgerx.dao.TransactionDao;
import io.github.vexisvx.ledgerx.model.Transaction;
import io.github.vexisvx.ledgerx.model.TransactionType;
import io.github.vexisvx.ledgerx.util.Validator;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionService {
    private final TransactionDao dao = new TransactionDao();

    public List<Transaction> getAll() { return dao.findAll(); }
    public void create(Transaction t) { validate(t); dao.insert(t); }
    public void update(Transaction t) { validate(t); dao.update(t); }
    public void delete(long id) { dao.delete(id); }
    public BigDecimal getTotalIncome() { return dao.sumByType(TransactionType.INCOME); }
    public BigDecimal getTotalExpense() { return dao.sumByType(TransactionType.EXPENSE); }
    public BigDecimal getNet() { return getTotalIncome().subtract(getTotalExpense()); }
    public BigDecimal getMonthIncome(int y, int m) { return dao.sumByTypeMonth(TransactionType.INCOME, y, m); }
    public BigDecimal getMonthExpense(int y, int m) { return dao.sumByTypeMonth(TransactionType.EXPENSE, y, m); }

    public List<Transaction> filter(List<Transaction> src, String kw, TransactionType ty, Long catId) {
        return src.stream()
            .filter(t -> kw == null || kw.isBlank()
                || t.getTitle().toLowerCase().contains(kw.toLowerCase())
                || (t.getDescription() != null && t.getDescription().toLowerCase().contains(kw.toLowerCase())))
            .filter(t -> ty == null || t.getType() == ty)
            .filter(t -> catId == null || catId.equals(t.getCategoryId()))
            .collect(Collectors.toList());
    }

    private void validate(Transaction t) {
        if (t.getType() == null)
            throw new IllegalArgumentException("Transaction type is required");
        Validator.validateAmount(t.getAmount());
        Validator.validateTitle(t.getTitle());
        Validator.validateDate(t.getDate());
        Validator.validateDescription(t.getDescription());
        if (t.getCategoryId() == null)
            throw new IllegalArgumentException("Category is required");
    }
}
