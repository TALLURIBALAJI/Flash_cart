package com.flashcart.service;

import com.flashcart.entities.*;
import com.flashcart.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.domain.PageRequest;

@Service
public class ExpenseService {
    
    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PaymentRepository paymentRepository;
    
    public ExpenseService(ExpenseRepository expenseRepository, ExpenseSplitRepository expenseSplitRepository,
                         UserRepository userRepository, GroupRepository groupRepository,
                         PaymentRepository paymentRepository) {
        this.expenseRepository = expenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.paymentRepository = paymentRepository;
    }
    
    @Transactional
    public Expense createAndSplitExpense(Group group, String description, String notes,
            BigDecimal totalAmount, User paidBy, List<User> splitUsers, SplitType splitType,
            ExpenseCategory category,
            List<com.flashcart.controller.ExpenseController.CreateExpenseRequest.SplitDetail> splitDetails) {
        
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Total amount must be greater than zero");
        if (splitUsers == null || splitUsers.isEmpty())
            throw new IllegalArgumentException("At least one user must be in the split");
        
        Expense expense = Expense.builder()
                .description(description).notes(notes).totalAmount(totalAmount)
                .group(group).paidBy(paidBy).splitType(splitType).category(category).build();
        expense = expenseRepository.save(expense);
        
        List<ExpenseSplit> splits;
        if (splitType == SplitType.EQUAL || splitType == null)
            splits = calculateEqualSplits(expense, totalAmount, splitUsers);
        else
            splits = calculateAdvancedSplits(expense, totalAmount, splitUsers, splitType, splitDetails);
        
        BigDecimal splitTotal = splits.stream().map(ExpenseSplit::getOweAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (splitTotal.compareTo(totalAmount) != 0) {
            log.error("Split error. Expected: {}, Got: {}", totalAmount, splitTotal);
            throw new RuntimeException("Expense split calculation failed");
        }
        
        splits.forEach(expenseSplitRepository::save);
        expense.setSplits(new HashSet<>(splits));
        log.info("Created expense {} for group {} with {} splits", expense.getId(), group.getId(), splits.size());
        return expense;
    }

    @Transactional
    public Expense updateExpense(Long expenseId, String description, String notes, ExpenseCategory category) {
        Expense expense = expenseRepository.findByIdAndIsActiveTrue(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        if (description != null) expense.setDescription(description);
        if (notes != null) expense.setNotes(notes);
        if (category != null) expense.setCategory(category);
        expense.setUpdatedAt(LocalDateTime.now());
        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(Long expenseId) {
        Expense expense = expenseRepository.findByIdAndIsActiveTrue(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        expense.setIsActive(false);
        expense.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense);
        log.info("Soft-deleted expense {}", expenseId);
    }
    
    private List<ExpenseSplit> calculateEqualSplits(Expense expense, BigDecimal totalAmount, List<User> splitUsers) {
        List<ExpenseSplit> splits = new ArrayList<>();
        int count = splitUsers.size();
        BigDecimal base = totalAmount.divide(new BigDecimal(count), 2, RoundingMode.DOWN);
        BigDecimal remainder = totalAmount.subtract(base.multiply(new BigDecimal(count)));
        int remainderCents = remainder.multiply(new BigDecimal(100)).intValue();
        
        for (int i = 0; i < count; i++) {
            BigDecimal amt = base;
            if (i < remainderCents) amt = amt.add(new BigDecimal("0.01"));
            splits.add(ExpenseSplit.builder().expense(expense).user(splitUsers.get(i)).oweAmount(amt).build());
        }
        return splits;
    }

    private List<ExpenseSplit> calculateAdvancedSplits(Expense expense, BigDecimal totalAmount,
            List<User> splitUsers, SplitType splitType,
            List<com.flashcart.controller.ExpenseController.CreateExpenseRequest.SplitDetail> splitDetails) {
        if (splitDetails == null || splitDetails.isEmpty())
            throw new IllegalArgumentException("Split details missing for advanced split");

        Map<Long, BigDecimal> detailMap = new HashMap<>();
        for (var d : splitDetails) detailMap.put(d.getUserId(), d.getValue());

        if (splitType == SplitType.EXACT) {
            List<ExpenseSplit> splits = new ArrayList<>();
            BigDecimal exactTotal = BigDecimal.ZERO;
            for (User u : splitUsers) {
                BigDecimal amt = detailMap.getOrDefault(u.getId(), BigDecimal.ZERO);
                exactTotal = exactTotal.add(amt);
                splits.add(ExpenseSplit.builder().expense(expense).user(u).oweAmount(amt).build());
            }
            if (exactTotal.compareTo(totalAmount) != 0)
                throw new IllegalArgumentException("Exact splits do not add up to total amount");
            return splits;
        }

        // PERCENTAGE or RATIO
        BigDecimal totalParts = BigDecimal.ZERO;
        for (BigDecimal p : detailMap.values()) totalParts = totalParts.add(p);
        if (splitType == SplitType.PERCENTAGE && totalParts.compareTo(new BigDecimal("100")) != 0)
            throw new IllegalArgumentException("Percentages must add up to 100%");

        BigDecimal distributed = BigDecimal.ZERO;
        List<ExpenseSplit> splits = new ArrayList<>();
        for (User u : splitUsers) {
            BigDecimal parts = detailMap.getOrDefault(u.getId(), BigDecimal.ZERO);
            BigDecimal amount = totalAmount.multiply(parts).divide(totalParts, 2, RoundingMode.DOWN);
            distributed = distributed.add(amount);
            splits.add(ExpenseSplit.builder().expense(expense).user(u).oweAmount(amount).build());
        }
        BigDecimal rem = totalAmount.subtract(distributed);
        int remCents = rem.multiply(new BigDecimal("100")).intValue();
        for (int i = 0; i < remCents && i < splits.size(); i++)
            splits.get(i).setOweAmount(splits.get(i).getOweAmount().add(new BigDecimal("0.01")));
        return splits;
    }
    
    @Transactional(readOnly = true)
    public List<Expense> getGroupExpenses(Long groupId) {
        Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        return expenseRepository.findByGroupAndIsActiveTrueOrderByExpenseDateDesc(group);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalOwes(Long userId) {
        return expenseSplitRepository.findByUserId(userId).stream()
                .map(ExpenseSplit::getOweAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalOwed(Long userId) {
        BigDecimal totalPaid = expenseRepository.findExpensesPaidByUser(userId).stream()
                .map(Expense::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalPaid.subtract(calculateTotalOwes(userId));
    }
    
    @Transactional(readOnly = true)
    public List<Expense> getRecentExpenses(Long userId) {
        List<Group> userGroups = groupRepository.findGroupsByMember(userId);
        if (userGroups.isEmpty()) return Collections.emptyList();
        return expenseRepository.findRecentExpenses(userGroups, PageRequest.of(0, 15));
    }
    
    @Transactional(readOnly = true)
    public List<Settlement> calculateSettlements(Long groupId) {
        Group group = groupRepository.findByIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        Map<Long, BigDecimal> balances = new HashMap<>();
        List<ExpenseSplit> splits = expenseSplitRepository.findByGroupId(groupId);
        for (ExpenseSplit split : splits) {
            balances.merge(split.getUser().getId(), split.getOweAmount().negate(), BigDecimal::add);
        }
        for (Expense expense : group.getExpenses()) {
            if (Boolean.TRUE.equals(expense.getIsActive()))
                balances.merge(expense.getPaidBy().getId(), expense.getTotalAmount(), BigDecimal::add);
        }
        // Factor in payments
        List<Payment> payments = paymentRepository.findByGroupId(groupId);
        for (Payment p : payments) {
            balances.merge(p.getPayer().getId(), p.getAmount().negate(), BigDecimal::add);
            balances.merge(p.getReceiver().getId(), p.getAmount(), BigDecimal::add);
        }
        
        List<Settlement> settlements = new ArrayList<>();
        List<Long> creditors = new ArrayList<>();
        List<Long> debtors = new ArrayList<>();
        for (var entry : balances.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) creditors.add(entry.getKey());
            else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) debtors.add(entry.getKey());
        }
        
        for (Long debtorId : debtors) {
            BigDecimal owed = balances.get(debtorId).abs();
            for (Long creditorId : creditors) {
                BigDecimal owedTo = balances.get(creditorId);
                if (owedTo.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal amt = owed.min(owedTo);
                    if (amt.compareTo(new BigDecimal("0.01")) >= 0) {
                        settlements.add(new Settlement(debtorId, creditorId, amt));
                    }
                    owed = owed.subtract(amt);
                    balances.put(creditorId, owedTo.subtract(amt));
                    if (owed.compareTo(BigDecimal.ZERO) == 0) break;
                }
            }
        }
        return settlements;
    }
    
    public static class Settlement {
        private Long payerId;
        private Long receiverId;
        private BigDecimal amount;
        
        public Settlement() {}
        public Settlement(Long payerId, Long receiverId, BigDecimal amount) {
            this.payerId = payerId; this.receiverId = receiverId; this.amount = amount;
        }
        public static SettlementBuilder builder() { return new SettlementBuilder(); }
        
        public static class SettlementBuilder {
            private Long payerId; private Long receiverId; private BigDecimal amount;
            public SettlementBuilder payerId(Long v) {this.payerId = v; return this;}
            public SettlementBuilder receiverId(Long v) {this.receiverId = v; return this;}
            public SettlementBuilder amount(BigDecimal v) {this.amount = v; return this;}
            public Settlement build() { return new Settlement(payerId, receiverId, amount); }
        }
        
        public Long getPayerId() {return payerId;}
        public Long getReceiverId() {return receiverId;}
        public BigDecimal getAmount() {return amount;}
    }
}
