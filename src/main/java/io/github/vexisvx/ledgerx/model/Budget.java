package io.github.vexisvx.ledgerx.model;

import java.math.BigDecimal;

public class Budget {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal monthlyLimit;
    private BigDecimal spent = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }
    public BigDecimal getSpent() { return spent; }
    public void setSpent(BigDecimal spent) { this.spent = spent; }
    public double getUsagePercent() {
        if (monthlyLimit == null || monthlyLimit.compareTo(BigDecimal.ZERO) == 0) return 0;
        return spent.divide(monthlyLimit, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }
}
