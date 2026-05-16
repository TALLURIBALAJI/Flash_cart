package com.flashcart.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ExpenseSplit Entity - Represents how much a user owes for a specific expense
 * 
 * Uses BigDecimal for strict financial accuracy with proper rounding.
 */
@Entity
@Table(name = "expense_splits", indexes = {
    @Index(name = "idx_split_expense", columnList = "expense_id"),
    @Index(name = "idx_split_user", columnList = "user_id")
})
public class ExpenseSplit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "owe_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal oweAmount;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    // No-args constructor
    public ExpenseSplit() {}
    
    // All-args constructor
    public ExpenseSplit(Long id, Expense expense, User user, BigDecimal oweAmount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.expense = expense;
        this.user = user;
        this.oweAmount = oweAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Builder
    public static ExpenseSplitBuilder builder() {
        return new ExpenseSplitBuilder();
    }
    
    public static class ExpenseSplitBuilder {
        private Long id;
        private Expense expense;
        private User user;
        private BigDecimal oweAmount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public ExpenseSplitBuilder id(Long id) {this.id = id; return this;}
        public ExpenseSplitBuilder expense(Expense expense) {this.expense = expense; return this;}
        public ExpenseSplitBuilder user(User user) {this.user = user; return this;}
        public ExpenseSplitBuilder oweAmount(BigDecimal oweAmount) {this.oweAmount = oweAmount; return this;}
        public ExpenseSplitBuilder createdAt(LocalDateTime createdAt) {this.createdAt = createdAt; return this;}
        public ExpenseSplitBuilder updatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt; return this;}
        
        public ExpenseSplit build() {
            return new ExpenseSplit(id, expense, user, oweAmount, createdAt, updatedAt);
        }
    }
    
    // Getters and Setters
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    
    public Expense getExpense() {return expense;}
    public void setExpense(Expense expense) {this.expense = expense;}
    
    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}
    
    public BigDecimal getOweAmount() {return oweAmount;}
    public void setOweAmount(BigDecimal oweAmount) {this.oweAmount = oweAmount;}
    
    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
    
    public LocalDateTime getUpdatedAt() {return updatedAt;}
    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
}
