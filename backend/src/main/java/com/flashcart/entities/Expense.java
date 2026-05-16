package com.flashcart.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Expense Entity - Represents a logged expense with category, notes, and split tracking
 */
@Entity
@Table(name = "expenses", indexes = {
    @Index(name = "idx_expense_group", columnList = "group_id"),
    @Index(name = "idx_expense_paid_by", columnList = "paid_by_id"),
    @Index(name = "idx_expense_date", columnList = "expense_date"),
    @Index(name = "idx_expense_category", columnList = "category")
})
public class Expense {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String description;

    @Column(length = 1000)
    private String notes;
    
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20)
    private ExpenseCategory category = ExpenseCategory.OTHER;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_id", nullable = false)
    private User paidBy;
    
    @OneToMany(mappedBy = "expense", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ExpenseSplit> splits = new HashSet<>();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "split_type", nullable = false)
    private SplitType splitType = SplitType.EQUAL;

    @Column(name = "expense_date", nullable = false)
    private LocalDateTime expenseDate = LocalDateTime.now();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // No-args constructor
    public Expense() {}
    
    // All-args constructor
    public Expense(Long id, String description, String notes, BigDecimal totalAmount, ExpenseCategory category, Group group, User paidBy, Set<ExpenseSplit> splits, SplitType splitType, LocalDateTime expenseDate, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean isActive) {
        this.id = id;
        this.description = description;
        this.notes = notes;
        this.totalAmount = totalAmount;
        this.category = category;
        this.group = group;
        this.paidBy = paidBy;
        this.splits = splits;
        this.splitType = splitType;
        this.expenseDate = expenseDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isActive = isActive;
    }
    
    // Builder
    public static ExpenseBuilder builder() {
        return new ExpenseBuilder();
    }
    
    public static class ExpenseBuilder {
        private Long id;
        private String description;
        private String notes;
        private BigDecimal totalAmount;
        private ExpenseCategory category = ExpenseCategory.OTHER;
        private Group group;
        private User paidBy;
        private Set<ExpenseSplit> splits = new HashSet<>();
        private SplitType splitType = SplitType.EQUAL;
        private LocalDateTime expenseDate = LocalDateTime.now();
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();
        private Boolean isActive;
        
        public ExpenseBuilder id(Long id) {this.id = id; return this;}
        public ExpenseBuilder description(String description) {this.description = description; return this;}
        public ExpenseBuilder notes(String notes) {this.notes = notes; return this;}
        public ExpenseBuilder totalAmount(BigDecimal totalAmount) {this.totalAmount = totalAmount; return this;}
        public ExpenseBuilder category(ExpenseCategory category) {this.category = category; return this;}
        public ExpenseBuilder group(Group group) {this.group = group; return this;}
        public ExpenseBuilder paidBy(User paidBy) {this.paidBy = paidBy; return this;}
        public ExpenseBuilder splits(Set<ExpenseSplit> splits) {this.splits = splits; return this;}
        public ExpenseBuilder splitType(SplitType splitType) {this.splitType = splitType; return this;}
        public ExpenseBuilder expenseDate(LocalDateTime expenseDate) {this.expenseDate = expenseDate; return this;}
        public ExpenseBuilder createdAt(LocalDateTime createdAt) {this.createdAt = createdAt; return this;}
        public ExpenseBuilder updatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt; return this;}
        public ExpenseBuilder isActive(Boolean isActive) {this.isActive = isActive; return this;}
        
        public Expense build() {
            return new Expense(id, description, notes, totalAmount, category, group, paidBy, splits, splitType, expenseDate, createdAt, updatedAt, isActive);
        }
    }
    
    // Getters and Setters
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public String getNotes() {return notes;}
    public void setNotes(String notes) {this.notes = notes;}
    
    public BigDecimal getTotalAmount() {return totalAmount;}
    public void setTotalAmount(BigDecimal totalAmount) {this.totalAmount = totalAmount;}

    public ExpenseCategory getCategory() {return category;}
    public void setCategory(ExpenseCategory category) {this.category = category;}
    
    public Group getGroup() {return group;}
    public void setGroup(Group group) {this.group = group;}
    
    public User getPaidBy() {return paidBy;}
    public void setPaidBy(User paidBy) {this.paidBy = paidBy;}
    
    public Set<ExpenseSplit> getSplits() {return splits;}
    public void setSplits(Set<ExpenseSplit> splits) {this.splits = splits;}

    public SplitType getSplitType() {return splitType;}
    public void setSplitType(SplitType splitType) {this.splitType = splitType;}
    
    public LocalDateTime getExpenseDate() {return expenseDate;}
    public void setExpenseDate(LocalDateTime expenseDate) {this.expenseDate = expenseDate;}
    
    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
    
    public LocalDateTime getUpdatedAt() {return updatedAt;}
    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
    
    public Boolean getIsActive() {return isActive;}
    public void setIsActive(Boolean isActive) {this.isActive = isActive;}
}
