package io.github.vexisvx.ledgerx.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDate date;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;

    public Transaction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public boolean isIncome() { return type == TransactionType.INCOME; }
}
