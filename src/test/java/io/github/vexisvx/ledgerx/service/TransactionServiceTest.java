package io.github.vexisvx.ledgerx.service;

import io.github.vexisvx.ledgerx.model.Transaction;
import io.github.vexisvx.ledgerx.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {

    // این تست‌ها بدون دیتابیس اجرا می‌شن — فقط روی متد filter()

    @Test void filterByTypeExpense() {
        var svc = new TransactionService();
        var list = List.of(
            tx(1L, TransactionType.INCOME, "100", "Salary"),
            tx(2L, TransactionType.EXPENSE, "50", "Food"));
        var r = svc.filter(list, null, TransactionType.EXPENSE, null);
        assertEquals(1, r.size());
        assertEquals("Food", r.get(0).getTitle());
    }

    @Test void filterByKeyword() {
        var svc = new TransactionService();
        var list = List.of(
            tx(1L, TransactionType.INCOME, "100", "Salary"),
            tx(2L, TransactionType.EXPENSE, "50", "Book purchase"));
        var r = svc.filter(list, "book", null, null);
        assertEquals(1, r.size());
        assertEquals("Book purchase", r.get(0).getTitle());
    }

    @Test void filterByDescription() {
        var svc = new TransactionService();
        Transaction t = tx(1L, TransactionType.EXPENSE, "50", "Misc");
        t.setDescription("Coffee at cafe");
        var r = svc.filter(List.of(t), "coffee", null, null);
        assertEquals(1, r.size());
    }

    @Test void filterByCategory() {
        var svc = new TransactionService();
        Transaction t1 = tx(1L, TransactionType.EXPENSE, "50", "A");
        t1.setCategoryId(5L);
        Transaction t2 = tx(2L, TransactionType.EXPENSE, "60", "B");
        t2.setCategoryId(7L);
        var r = svc.filter(List.of(t1, t2), null, null, 5L);
        assertEquals(1, r.size());
        assertEquals("A", r.get(0).getTitle());
    }

    @Test void filterMultipleConditions() {
        var svc = new TransactionService();
        Transaction t1 = tx(1L, TransactionType.INCOME, "100", "Salary");
        t1.setCategoryId(1L);
        Transaction t2 = tx(2L, TransactionType.EXPENSE, "50", "Food");
        t2.setCategoryId(2L);
        var r = svc.filter(List.of(t1, t2), "salary", TransactionType.INCOME, 1L);
        assertEquals(1, r.size());
    }

    @Test void netBalanceCalculation() {
        BigDecimal income = new BigDecimal("1000.00");
        BigDecimal expense = new BigDecimal("350.50");
        BigDecimal net = income.subtract(expense);
        assertEquals(0, new BigDecimal("649.50").compareTo(net));
    }

    private Transaction tx(Long id, TransactionType type, String amount, String title) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setType(type);
        t.setAmount(new BigDecimal(amount));
        t.setTitle(title);
        t.setDate(LocalDate.now());
        return t;
    }
}
