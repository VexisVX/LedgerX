package io.github.vexisvx.ledgerx.service;

import io.github.vexisvx.ledgerx.dao.RecurringDao;
import io.github.vexisvx.ledgerx.dao.TransactionDao;
import io.github.vexisvx.ledgerx.model.RecurringTransaction;
import io.github.vexisvx.ledgerx.model.Transaction;
import io.github.vexisvx.ledgerx.util.Validator;

import java.time.LocalDate;
import java.util.List;

public class RecurringService {
    private final RecurringDao dao = new RecurringDao();
    private final TransactionDao txDao = new TransactionDao();

    public List<RecurringTransaction> getAll() { return dao.findAll(); }

    public void create(RecurringTransaction r) {
        validate(r);
        dao.insert(r);
    }

    public void update(RecurringTransaction r) {
        validate(r);
        dao.update(r);
    }

    public void delete(long id) { dao.delete(id); }

    /**
     * Processes all due recurring transactions:
     *  - Creates a normal Transaction for each
     *  - Advances nextDate by frequency
     * Returns number of transactions created.
     */
    public int processDue() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> due = dao.findDue(today);
        int count = 0;
        for (RecurringTransaction r : due) {
            Transaction t = new Transaction();
            t.setType(r.getType());
            t.setAmount(r.getAmount());
            t.setDate(r.getNextDate());
            t.setTitle(r.getTitle());
            t.setDescription(r.getDescription() != null ? r.getDescription() : "");
            t.setCategoryId(r.getCategoryId());
            txDao.insert(t);

            LocalDate next = advance(r.getNextDate(), r.getFrequency());
            while (!next.isAfter(today)) {
                next = advance(next, r.getFrequency());
            }
            r.setNextDate(next);
            if (r.getEndDate() != null && next.isAfter(r.getEndDate())) r.setActive(false);
            dao.update(r);
            count++;
        }
        return count;
    }

    private LocalDate advance(LocalDate d, RecurringTransaction.Frequency f) {
        return switch (f) {
            case DAILY -> d.plusDays(1);
            case WEEKLY -> d.plusWeeks(1);
            case MONTHLY -> d.plusMonths(1);
            case YEARLY -> d.plusYears(1);
        };
    }

    private void validate(RecurringTransaction r) {
        Validator.validateAmount(r.getAmount());
        Validator.validateTitle(r.getTitle());
        // recurring uses FUTURE date (nextDate), not past date
        Validator.validateFutureDate(r.getNextDate());
        if (r.getFrequency() == null)
            throw new IllegalArgumentException("Frequency is required");
    }

    /**
     * Count of active recurring items that are due (nextDate <= today).
     * Does NOT mutate state, does NOT create transactions.
     */
    public int getDueCount() {
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            return dao.findDue(today).size();
        } catch (Exception e) {
            return 0;
        }
    }
}
