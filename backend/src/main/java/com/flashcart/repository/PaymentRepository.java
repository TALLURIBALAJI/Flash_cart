package com.flashcart.repository;

import com.flashcart.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Payment Repository - Database operations for Payment entity
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p WHERE p.group.id = :groupId ORDER BY p.createdAt DESC")
    List<Payment> findByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT p FROM Payment p WHERE p.payer.id = :userId OR p.receiver.id = :userId ORDER BY p.createdAt DESC")
    List<Payment> findByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Payment p WHERE p.group.id = :groupId AND (p.payer.id = :userId OR p.receiver.id = :userId) ORDER BY p.createdAt DESC")
    List<Payment> findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
