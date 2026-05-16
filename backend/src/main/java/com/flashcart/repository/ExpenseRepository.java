package com.flashcart.repository;

import com.flashcart.entities.Expense;
import com.flashcart.entities.Group;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Expense Repository - Database operations for Expense entity
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    List<Expense> findByGroupAndIsActiveTrueOrderByExpenseDateDesc(Group group);
    
    @Query("SELECT e FROM Expense e WHERE e.group.id = :groupId AND e.isActive = true ORDER BY e.expenseDate DESC")
    List<Expense> findByGroupIdOrderByDate(@Param("groupId") Long groupId);
    
    @Query("SELECT e FROM Expense e WHERE e.paidBy.id = :userId AND e.isActive = true")
    List<Expense> findExpensesPaidByUser(@Param("userId") Long userId);
    
    @Query("SELECT e FROM Expense e WHERE e.group IN :groups AND e.isActive = true ORDER BY e.expenseDate DESC")
    List<Expense> findRecentExpenses(@Param("groups") List<Group> groups, Pageable pageable);
    
    Optional<Expense> findByIdAndIsActiveTrue(Long id);
}
