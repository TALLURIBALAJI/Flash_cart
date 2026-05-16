package com.flashcart.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Group Entity - Represents a shared expense group with type categorization
 */
@Entity
@Table(name = "expense_groups")
public class Group {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Group name is required")
    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", length = 20)
    private GroupType groupType = GroupType.OTHER;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;
    
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();
    
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Expense> expenses = new HashSet<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // No-args constructor
    public Group() {}
    
    // All-args constructor
    public Group(Long id, String name, String description, GroupType groupType, User createdBy, Set<User> members, Set<Expense> expenses, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.groupType = groupType;
        this.createdBy = createdBy;
        this.members = members;
        this.expenses = expenses;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isActive = isActive;
    }
    
    // Builder
    public static GroupBuilder builder() {
        return new GroupBuilder();
    }
    
    public static class GroupBuilder {
        private Long id;
        private String name;
        private String description;
        private GroupType groupType = GroupType.OTHER;
        private User createdBy;
        private Set<User> members = new HashSet<>();
        private Set<Expense> expenses = new HashSet<>();
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();
        private Boolean isActive;
        
        public GroupBuilder id(Long id) {this.id = id; return this;}
        public GroupBuilder name(String name) {this.name = name; return this;}
        public GroupBuilder description(String description) {this.description = description; return this;}
        public GroupBuilder groupType(GroupType groupType) {this.groupType = groupType; return this;}
        public GroupBuilder createdBy(User createdBy) {this.createdBy = createdBy; return this;}
        public GroupBuilder members(Set<User> members) {this.members = members; return this;}
        public GroupBuilder expenses(Set<Expense> expenses) {this.expenses = expenses; return this;}
        public GroupBuilder createdAt(LocalDateTime createdAt) {this.createdAt = createdAt; return this;}
        public GroupBuilder updatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt; return this;}
        public GroupBuilder isActive(Boolean isActive) {this.isActive = isActive; return this;}
        
        public Group build() {
            return new Group(id, name, description, groupType, createdBy, members, expenses, createdAt, updatedAt, isActive);
        }
    }
    
    // Getters and Setters
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    public GroupType getGroupType() {return groupType;}
    public void setGroupType(GroupType groupType) {this.groupType = groupType;}
    
    public User getCreatedBy() {return createdBy;}
    public void setCreatedBy(User createdBy) {this.createdBy = createdBy;}
    
    public Set<User> getMembers() {return members;}
    public void setMembers(Set<User> members) {this.members = members;}
    
    public Set<Expense> getExpenses() {return expenses;}
    public void setExpenses(Set<Expense> expenses) {this.expenses = expenses;}
    
    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
    
    public LocalDateTime getUpdatedAt() {return updatedAt;}
    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
    
    public Boolean getIsActive() {return isActive;}
    public void setIsActive(Boolean isActive) {this.isActive = isActive;}
}
