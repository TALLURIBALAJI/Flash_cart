package com.flashcart.repository;

import com.flashcart.entities.Expense;
import com.flashcart.entities.ExpenseSplit;
import com.flashcart.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ExpenseSplit Repository - Database operations for ExpenseSplit entity
 */
@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {
    
    List<ExpenseSplit> findByExpense(Expense expense);
    
    @Query("SELECT es FROM ExpenseSplit es WHERE es.user.id = :userId")
    List<ExpenseSplit> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT es FROM ExpenseSplit es WHERE es.expense.group.id = :groupId")
    List<ExpenseSplit> findByGroupId(@Param("groupId") Long groupId);
    
    Optional<ExpenseSplit> findByExpenseAndUser(Expense expense, User user);
}
