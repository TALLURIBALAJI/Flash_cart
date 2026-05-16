package com.flashcart.controller;

import com.flashcart.entities.ExpenseCategory;
import com.flashcart.entities.GroupType;
import com.flashcart.entities.Payment;
import com.flashcart.service.ExpenseService;
import com.flashcart.service.GroupService;
import com.flashcart.service.PaymentService;
import com.flashcart.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {
    
    private static final Logger log = LoggerFactory.getLogger(SettlementController.class);
    private final ExpenseService expenseService;
    private final GroupService groupService;
    private final UserService userService;
    private final PaymentService paymentService;
    
    public SettlementController(ExpenseService expenseService, GroupService groupService, 
                                UserService userService, PaymentService paymentService) {
        this.expenseService = expenseService;
        this.groupService = groupService;
        this.userService = userService;
        this.paymentService = paymentService;
    }
    
    @GetMapping
    public ResponseEntity<?> getSettlements(@RequestAttribute("userEmail") String userEmail) {
        try {
            var user = userService.getUserByEmail(userEmail);
            var groups = groupService.getUserGroups(user.getId());
            
            List<Map<String, Object>> all = new ArrayList<>();
            for (var group : groups) {
                var settlements = expenseService.calculateSettlements(group.getId());
                for (var s : settlements) {
                    var payer = userService.getUserById(s.getPayerId());
                    var receiver = userService.getUserById(s.getReceiverId());
                    Map<String, Object> m = new HashMap<>();
                    m.put("payerId", s.getPayerId());
                    m.put("payerEmail", payer.getEmail());
                    m.put("payerName", payer.getUsername());
                    m.put("receiverId", s.getReceiverId());
                    m.put("receiverEmail", receiver.getEmail());
                    m.put("receiverName", receiver.getUsername());
                    m.put("amount", s.getAmount());
                    m.put("groupId", group.getId());
                    m.put("groupName", group.getName());
                    all.add(m);
                }
            }
            return ResponseEntity.ok(all);
        } catch (Exception e) {
            log.error("Failed to fetch settlements", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroupSettlements(@PathVariable Long groupId) {
        try {
            var settlements = expenseService.calculateSettlements(groupId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (var s : settlements) {
                var payer = userService.getUserById(s.getPayerId());
                var receiver = userService.getUserById(s.getReceiverId());
                Map<String, Object> m = new HashMap<>();
                m.put("payerId", s.getPayerId());
                m.put("payerEmail", payer.getEmail());
                m.put("payerName", payer.getUsername());
                m.put("receiverId", s.getReceiverId());
                m.put("receiverEmail", receiver.getEmail());
                m.put("receiverName", receiver.getUsername());
                m.put("amount", s.getAmount());
                result.add(m);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch group settlements", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /** Record a settlement payment */
    @PostMapping("/pay")
    public ResponseEntity<?> recordPayment(@RequestBody PaymentRequest req, @RequestAttribute("userEmail") String email) {
        try {
            Payment payment = paymentService.recordPayment(req.getPayerId(), req.getReceiverId(), req.getGroupId(), req.getAmount(), req.getNote());
            Map<String, Object> resp = new HashMap<>();
            resp.put("id", payment.getId());
            resp.put("payerName", payment.getPayer().getUsername());
            resp.put("receiverName", payment.getReceiver().getUsername());
            resp.put("amount", payment.getAmount());
            resp.put("createdAt", payment.getCreatedAt());
            resp.put("message", "Payment recorded successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            log.error("Failed to record payment", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /** Get payment history for a group */
    @GetMapping("/payments/group/{groupId}")
    public ResponseEntity<?> getGroupPayments(@PathVariable Long groupId) {
        try {
            var payments = paymentService.getGroupPayments(groupId);
            List<Map<String, Object>> result = new ArrayList<>();
            for (var p : payments) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getId());
                m.put("payerName", p.getPayer().getUsername());
                m.put("payerEmail", p.getPayer().getEmail());
                m.put("receiverName", p.getReceiver().getUsername());
                m.put("receiverEmail", p.getReceiver().getEmail());
                m.put("amount", p.getAmount());
                m.put("note", p.getNote());
                m.put("createdAt", p.getCreatedAt());
                result.add(m);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /** Categories endpoint (public) */
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        List<Map<String, String>> cats = new ArrayList<>();
        for (ExpenseCategory c : ExpenseCategory.values()) {
            cats.add(Map.of("name", c.name(), "icon", c.getIcon(), "label", c.getLabel()));
        }
        return ResponseEntity.ok(cats);
    }
    
    public static class PaymentRequest {
        private Long payerId;
        private Long receiverId;
        private Long groupId;
        private BigDecimal amount;
        private String note;
        
        public PaymentRequest() {}
        public Long getPayerId() {return payerId;}
        public void setPayerId(Long payerId) {this.payerId = payerId;}
        public Long getReceiverId() {return receiverId;}
        public void setReceiverId(Long receiverId) {this.receiverId = receiverId;}
        public Long getGroupId() {return groupId;}
        public void setGroupId(Long groupId) {this.groupId = groupId;}
        public BigDecimal getAmount() {return amount;}
        public void setAmount(BigDecimal amount) {this.amount = amount;}
        public String getNote() {return note;}
        public void setNote(String note) {this.note = note;}
    }
}
