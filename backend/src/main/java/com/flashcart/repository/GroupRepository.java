package com.flashcart.repository;

import com.flashcart.entities.Group;
import com.flashcart.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Group Repository - Database operations for Group entity
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.id = :userId AND g.isActive = true")
    List<Group> findGroupsByMember(@Param("userId") Long userId);
    
    @Query("SELECT g FROM Group g WHERE g.createdBy.id = :userId AND g.isActive = true")
    List<Group> findGroupsCreatedBy(@Param("userId") Long userId);
    
    Optional<Group> findByIdAndIsActiveTrue(Long id);
}
