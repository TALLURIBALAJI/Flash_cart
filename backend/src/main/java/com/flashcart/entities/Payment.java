package com.flashcart.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Entity - Records actual settlement payments between users.
 * This surpasses PhonePe by keeping a full history of who paid whom and when.
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_payer", columnList = "payer_id"),
    @Index(name = "idx_payment_receiver", columnList = "receiver_id"),
    @Index(name = "idx_payment_group", columnList = "group_id")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Payment() {}

    public Payment(Long id, User payer, User receiver, BigDecimal amount, Group group, String note, LocalDateTime createdAt) {
        this.id = id;
        this.payer = payer;
        this.receiver = receiver;
        this.amount = amount;
        this.group = group;
        this.note = note;
        this.createdAt = createdAt;
    }

    public static PaymentBuilder builder() { return new PaymentBuilder(); }

    public static class PaymentBuilder {
        private Long id;
        private User payer;
        private User receiver;
        private BigDecimal amount;
        private Group group;
        private String note;
        private LocalDateTime createdAt = LocalDateTime.now();

        public PaymentBuilder id(Long id) {this.id = id; return this;}
        public PaymentBuilder payer(User payer) {this.payer = payer; return this;}
        public PaymentBuilder receiver(User receiver) {this.receiver = receiver; return this;}
        public PaymentBuilder amount(BigDecimal amount) {this.amount = amount; return this;}
        public PaymentBuilder group(Group group) {this.group = group; return this;}
        public PaymentBuilder note(String note) {this.note = note; return this;}
        public PaymentBuilder createdAt(LocalDateTime createdAt) {this.createdAt = createdAt; return this;}

        public Payment build() {
            return new Payment(id, payer, receiver, amount, group, note, createdAt);
        }
    }

    // Getters and Setters
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public User getPayer() {return payer;}
    public void setPayer(User payer) {this.payer = payer;}
    public User getReceiver() {return receiver;}
    public void setReceiver(User receiver) {this.receiver = receiver;}
    public BigDecimal getAmount() {return amount;}
    public void setAmount(BigDecimal amount) {this.amount = amount;}
    public Group getGroup() {return group;}
    public void setGroup(Group group) {this.group = group;}
    public String getNote() {return note;}
    public void setNote(String note) {this.note = note;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
}
