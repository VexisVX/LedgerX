package io.github.vexisvx.ledgerx.service;

import io.github.vexisvx.ledgerx.dao.TransactionDao;
import io.github.vexisvx.ledgerx.model.Transaction;
import io.github.vexisvx.ledgerx.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class BalanceTimelineService {

    public static class Entry {
        public final LocalDate date;
        public final BigDecimal balance;
        public final Transaction transaction;

        public Entry(LocalDate date, BigDecimal balance, Transaction transaction) {
            this.date = date;
            this.balance = balance;
            this.transaction = transaction;
        }
    }

    public static class Timeline {
        public final BigDecimal startBalance;
        public final BigDecimal endBalance;
        public final LocalDate startDate;
        public final LocalDate endDate;
        public final List<Entry> entries;

        public Timeline(BigDecimal startBalance, BigDecimal endBalance,
                        LocalDate startDate, LocalDate endDate, List<Entry> entries) {
            this.startBalance = startBalance;
            this.endBalance = endBalance;
            this.startDate = startDate;
            this.endDate = endDate;
            this.entries = entries;
        }

        public BigDecimal netChange() {
            return endBalance.subtract(startBalance);
        }
    }

    private final TransactionDao dao = new TransactionDao();

    /** Returns timeline for the given date range. */
    public Timeline buildTimeline(LocalDate startDate, LocalDate endDate) {
        List<Transaction> all = dao.findAll();
        // Sort by date ascending
        all.sort(Comparator.comparing(Transaction::getDate).thenComparing(Transaction::getId));

        // Find balance at start of period (sum of all before startDate)
        BigDecimal startBalance = BigDecimal.ZERO;
        for (Transaction t : all) {
            if (t.getDate().isBefore(startDate)) {
                startBalance = startBalance.add(signedAmount(t));
            }
        }

        // Build entries within range
        List<Entry> entries = new ArrayList<>();
        BigDecimal running = startBalance;
        for (Transaction t : all) {
            if (t.getDate().isBefore(startDate) || t.getDate().isAfter(endDate)) continue;
            running = running.add(signedAmount(t));
            entries.add(new Entry(t.getDate(), running, t));
        }

        return new Timeline(startBalance, running, startDate, endDate, entries);
    }

    /** Returns timeline for last N days. */
    public Timeline buildTimeline(int daysBack) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(daysBack);
        return buildTimeline(start, end);
    }

    private BigDecimal signedAmount(Transaction t) {
        return t.getType() == TransactionType.INCOME ? t.getAmount() : t.getAmount().negate();
    }
}
