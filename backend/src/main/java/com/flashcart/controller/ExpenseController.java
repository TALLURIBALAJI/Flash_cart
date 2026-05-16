package com.flashcart.controller;

import com.flashcart.entities.*;
import com.flashcart.service.ExpenseService;
import com.flashcart.service.GroupService;
import com.flashcart.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Expense Controller - REST endpoints for expense management (JWT-secured)
 */
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    
    private static final Logger log = LoggerFactory.getLogger(ExpenseController.class);
    private final ExpenseService expenseService;
    private final GroupService groupService;
    private final UserService userService;
    
    public ExpenseController(ExpenseService expenseService, GroupService groupService, UserService userService) {
        this.expenseService = expenseService;
        this.groupService = groupService;
        this.userService = userService;
    }
    
    /**
     * Create a new expense and split it among group members
     * POST /api/expenses
     */
    @PostMapping
    public ResponseEntity<?> createExpense(
            @RequestBody CreateExpenseRequest request,
            @RequestAttribute("userEmail") String userEmail) {
        try {
            if (request.getTotalAmount() == null || request.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Total amount must be greater than zero"));
            }
            
            if (request.getSplitUserIds() == null || request.getSplitUserIds().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "At least one user must be selected for split"));
            }
            
            Group group = groupService.getGroupById(request.getGroupId());
            User paidBy = userService.getUserById(request.getPaidById());
            
            List<User> splitUsers = request.getSplitUserIds().stream()
                    .map(userService::getUserById)
                    .collect(Collectors.toList());
            
            Expense expense = expenseService.createAndSplitExpense(
                    group,
                    request.getDescription(),
                    request.getNotes(),
                    request.getTotalAmount(),
                    paidBy,
                    splitUsers,
                    request.getSplitType() != null ? request.getSplitType() : SplitType.EQUAL,
                    request.getCategory() != null ? request.getCategory() : ExpenseCategory.OTHER,
                    request.getSplitDetails()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(mapExpenseToResponse(expense));
        } catch (Exception e) {
            log.error("Failed to create expense", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update an existing expense
     * PUT /api/expenses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(
            @PathVariable Long id,
            @RequestBody UpdateExpenseRequest request,
            @RequestAttribute("userEmail") String userEmail) {
        try {
            Expense expense = expenseService.updateExpense(id, request.getDescription(), request.getNotes(), request.getCategory());
            return ResponseEntity.ok(mapExpenseToResponse(expense));
        } catch (Exception e) {
            log.error("Failed to update expense", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Soft-delete an expense
     * DELETE /api/expenses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id, @RequestAttribute("userEmail") String userEmail) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.ok(Map.of("message", "Expense deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete expense", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get expenses for a specific group
     * GET /api/expenses/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroupExpenses(@PathVariable Long groupId) {
        try {
            List<Expense> expenses = expenseService.getGroupExpenses(groupId);
            List<Map<String, Object>> response = expenses.stream()
                    .map(this::mapExpenseToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch group expenses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get recent expenses for the current user
     * GET /api/expenses/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentExpenses(@RequestAttribute("userEmail") String userEmail) {
        try {
            User user = userService.getUserByEmail(userEmail);
            List<Expense> expenses = expenseService.getRecentExpenses(user.getId());
            
            List<Map<String, Object>> response = expenses.stream()
                    .map(this::mapExpenseToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch recent expenses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    private Map<String, Object> mapExpenseToResponse(Expense expense) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", expense.getId());
        response.put("description", expense.getDescription());
        response.put("notes", expense.getNotes());
        response.put("totalAmount", expense.getTotalAmount());
        response.put("category", expense.getCategory());
        response.put("categoryIcon", expense.getCategory() != null ? expense.getCategory().getIcon() : "📦");
        response.put("categoryLabel", expense.getCategory() != null ? expense.getCategory().getLabel() : "Other");
        response.put("splitType", expense.getSplitType());
        response.put("paidById", expense.getPaidBy().getId());
        response.put("paidByName", expense.getPaidBy().getUsername());
        response.put("paidByEmail", expense.getPaidBy().getEmail());
        response.put("groupId", expense.getGroup().getId());
        response.put("groupName", expense.getGroup().getName());
        response.put("date", expense.getExpenseDate());
        response.put("createdAt", expense.getCreatedAt());
        
        List<Map<String, Object>> splits = expense.getSplits().stream()
                .map(split -> {
                    Map<String, Object> s = new HashMap<>();
                    s.put("userId", split.getUser().getId());
                    s.put("userEmail", split.getUser().getEmail());
                    s.put("userName", split.getUser().getUsername());
                    s.put("oweAmount", split.getOweAmount());
                    return s;
                })
                .collect(Collectors.toList());
        response.put("splits", splits);
        
        return response;
    }
    
    // Request DTOs
    public static class CreateExpenseRequest {
        private Long groupId;
        private String description;
        private String notes;
        private BigDecimal totalAmount;
        private Long paidById;
        private List<Long> splitUserIds;
        private SplitType splitType;
        private ExpenseCategory category;
        private List<SplitDetail> splitDetails;
        
        public CreateExpenseRequest() {}
        
        public static class SplitDetail {
            private Long userId;
            private BigDecimal value;
            
            public SplitDetail() {}
            public Long getUserId() { return userId; }
            public void setUserId(Long userId) { this.userId = userId; }
            public BigDecimal getValue() { return value; }
            public void setValue(BigDecimal value) { this.value = value; }
        }

        public Long getGroupId() {return groupId;}
        public void setGroupId(Long groupId) {this.groupId = groupId;}
        public String getDescription() {return description;}
        public void setDescription(String description) {this.description = description;}
        public String getNotes() {return notes;}
        public void setNotes(String notes) {this.notes = notes;}
        public BigDecimal getTotalAmount() {return totalAmount;}
        public void setTotalAmount(BigDecimal totalAmount) {this.totalAmount = totalAmount;}
        public Long getPaidById() {return paidById;}
        public void setPaidById(Long paidById) {this.paidById = paidById;}
        public List<Long> getSplitUserIds() {return splitUserIds;}
        public void setSplitUserIds(List<Long> splitUserIds) {this.splitUserIds = splitUserIds;}
        public SplitType getSplitType() {return splitType;}
        public void setSplitType(SplitType splitType) {this.splitType = splitType;}
        public ExpenseCategory getCategory() {return category;}
        public void setCategory(ExpenseCategory category) {this.category = category;}
        public List<SplitDetail> getSplitDetails() {return splitDetails;}
        public void setSplitDetails(List<SplitDetail> splitDetails) {this.splitDetails = splitDetails;}
    }

    public static class UpdateExpenseRequest {
        private String description;
        private String notes;
        private ExpenseCategory category;

        public UpdateExpenseRequest() {}
        public String getDescription() {return description;}
        public void setDescription(String description) {this.description = description;}
        public String getNotes() {return notes;}
        public void setNotes(String notes) {this.notes = notes;}
        public ExpenseCategory getCategory() {return category;}
        public void setCategory(ExpenseCategory category) {this.category = category;}
    }
}
