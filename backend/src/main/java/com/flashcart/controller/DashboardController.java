package com.flashcart.controller;

import com.flashcart.entities.Expense;
import com.flashcart.entities.ExpenseCategory;
import com.flashcart.service.ExpenseService;
import com.flashcart.service.GroupService;
import com.flashcart.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);
    private final ExpenseService expenseService;
    private final GroupService groupService;
    private final UserService userService;
    
    public DashboardController(ExpenseService expenseService, GroupService groupService, UserService userService) {
        this.expenseService = expenseService;
        this.groupService = groupService;
        this.userService = userService;
    }
    
    @GetMapping
    public ResponseEntity<?> getDashboard(@RequestAttribute("userEmail") String userEmail) {
        try {
            var user = userService.getUserByEmail(userEmail);
            var groups = groupService.getUserGroups(user.getId());
            var recentExpenses = expenseService.getRecentExpenses(user.getId());
            var totalOwes = expenseService.calculateTotalOwes(user.getId());
            var totalOwed = expenseService.calculateTotalOwed(user.getId());
            
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalOwes", totalOwes);
            dashboard.put("totalOwed", totalOwed);
            dashboard.put("netBalance", totalOwed.subtract(totalOwes));
            dashboard.put("groupCount", groups.size());
            dashboard.put("currency", user.getCurrency() != null ? user.getCurrency() : "INR");
            
            int expCount = 0;
            Map<String, BigDecimal> catSpending = new HashMap<>();
            for (var g : groups) {
                for (var e : g.getExpenses()) {
                    if (Boolean.TRUE.equals(e.getIsActive())) {
                        expCount++;
                        String cat = e.getCategory() != null ? e.getCategory().name() : "OTHER";
                        catSpending.merge(cat, e.getTotalAmount(), BigDecimal::add);
                    }
                }
            }
            dashboard.put("expenseCount", expCount);
            
            List<Map<String, Object>> catBreakdown = new ArrayList<>();
            for (var entry : catSpending.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                try {
                    ExpenseCategory ec = ExpenseCategory.valueOf(entry.getKey());
                    item.put("category", ec.name());
                    item.put("icon", ec.getIcon());
                    item.put("label", ec.getLabel());
                } catch (Exception ex) {
                    item.put("category", entry.getKey());
                    item.put("icon", "📦");
                    item.put("label", "Other");
                }
                item.put("amount", entry.getValue());
                catBreakdown.add(item);
            }
            catBreakdown.sort((a, b) -> ((BigDecimal)b.get("amount")).compareTo((BigDecimal)a.get("amount")));
            dashboard.put("categoryBreakdown", catBreakdown);
            
            List<Map<String, Object>> recentList = new ArrayList<>();
            for (Expense exp : recentExpenses.stream().limit(10).toList()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", exp.getId());
                m.put("description", exp.getDescription());
                m.put("totalAmount", exp.getTotalAmount());
                m.put("category", exp.getCategory());
                m.put("categoryIcon", exp.getCategory() != null ? exp.getCategory().getIcon() : "📦");
                m.put("paidByName", exp.getPaidBy().getUsername());
                m.put("paidById", exp.getPaidBy().getId());
                m.put("groupName", exp.getGroup().getName());
                m.put("groupId", exp.getGroup().getId());
                m.put("date", exp.getExpenseDate());
                recentList.add(m);
            }
            dashboard.put("recentExpenses", recentList);
            
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Failed to fetch dashboard", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
