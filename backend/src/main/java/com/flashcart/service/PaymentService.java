package com.flashcart.service;

import com.flashcart.entities.Group;
import com.flashcart.entities.Payment;
import com.flashcart.entities.User;
import com.flashcart.repository.GroupRepository;
import com.flashcart.repository.PaymentRepository;
import com.flashcart.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository, GroupRepository groupRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional
    public Payment recordPayment(Long payerId, Long receiverId, Long groupId, BigDecimal amount, String note) {
        User payer = userRepository.findById(payerId).orElseThrow(() -> new RuntimeException("Payer not found"));
        User receiver = userRepository.findById(receiverId).orElseThrow(() -> new RuntimeException("Receiver not found"));
        Group group = groupRepository.findByIdAndIsActiveTrue(groupId).orElseThrow(() -> new RuntimeException("Group not found"));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        Payment payment = Payment.builder()
                .payer(payer)
                .receiver(receiver)
                .group(group)
                .amount(amount)
                .note(note)
                .build();
        payment = paymentRepository.save(payment);
        log.info("Recorded payment: {} -> {} for {} in group {}", payerId, receiverId, amount, groupId);
        return payment;
    }

    @Transactional(readOnly = true)
    public List<Payment> getGroupPayments(Long groupId) {
        return paymentRepository.findByGroupId(groupId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getUserPayments(Long userId) {
        return paymentRepository.findByUserId(userId);
    }
}
