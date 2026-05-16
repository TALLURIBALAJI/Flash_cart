package com.flashcart.controller;

import com.flashcart.entities.*;
import com.flashcart.service.GroupService;
import com.flashcart.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Group Controller - REST endpoints for group management (JWT-secured)
 */
@RestController
@RequestMapping("/api/groups")
public class GroupController {
    
    private static final Logger log = LoggerFactory.getLogger(GroupController.class);
    private final GroupService groupService;
    private final UserService userService;
    
    public GroupController(GroupService groupService, UserService userService) {
        this.groupService = groupService;
        this.userService = userService;
    }
    
    /**
     * Create a new group
     * POST /api/groups
     */
    @PostMapping
    public ResponseEntity<?> createGroup(
            @RequestBody CreateGroupRequest request,
            @RequestAttribute("userEmail") String userEmail) {
        try {
            User creator = userService.getUserByEmail(userEmail);
            Group group = groupService.createGroup(
                    request.getName(),
                    request.getDescription(),
                    request.getGroupType() != null ? request.getGroupType() : GroupType.OTHER,
                    creator,
                    request.getMemberEmails()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(mapGroupToResponse(group));
        } catch (Exception e) {
            log.error("Failed to create group", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get all groups for the current user
     * GET /api/groups
     */
    @GetMapping
    public ResponseEntity<?> getUserGroups(@RequestAttribute("userEmail") String userEmail) {
        try {
            User user = userService.getUserByEmail(userEmail);
            List<Group> groups = groupService.getUserGroups(user.getId());
            
            List<Map<String, Object>> response = groups.stream()
                    .map(this::mapGroupToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch groups", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get a specific group by ID
     * GET /api/groups/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getGroup(@PathVariable Long id) {
        try {
            Group group = groupService.getGroupById(id);
            return ResponseEntity.ok(mapGroupToResponse(group));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Group not found"));
        }
    }
    
    /**
     * Add a member to a group
     * POST /api/groups/{id}/members
     */
    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(
            @PathVariable Long id,
            @RequestBody AddMemberRequest request) {
        try {
            Group group = groupService.addMemberToGroup(id, request.getEmail());
            return ResponseEntity.ok(mapGroupToResponse(group));
        } catch (Exception e) {
            log.error("Failed to add member", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    private Map<String, Object> mapGroupToResponse(Group group) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", group.getId());
        response.put("name", group.getName());
        response.put("description", group.getDescription());
        response.put("groupType", group.getGroupType());
        response.put("groupTypeIcon", group.getGroupType() != null ? group.getGroupType().getIcon() : "📁");
        response.put("groupTypeLabel", group.getGroupType() != null ? group.getGroupType().getLabel() : "Other");
        response.put("memberCount", group.getMembers().size());
        response.put("expenseCount", group.getExpenses().size());
        response.put("createdAt", group.getCreatedAt());
        
        List<Map<String, Object>> members = group.getMembers().stream()
                .map(member -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", member.getId());
                    m.put("email", member.getEmail());
                    m.put("username", member.getUsername());
                    return m;
                })
                .collect(Collectors.toList());
        response.put("members", members);
        
        return response;
    }
    
    // Request DTOs
    public static class CreateGroupRequest {
        private String name;
        private String description;
        private GroupType groupType;
        private List<String> memberEmails;
        
        public CreateGroupRequest() {}
        
        public String getName() {return name;}
        public void setName(String name) {this.name = name;}
        public String getDescription() {return description;}
        public void setDescription(String description) {this.description = description;}
        public GroupType getGroupType() {return groupType;}
        public void setGroupType(GroupType groupType) {this.groupType = groupType;}
        public List<String> getMemberEmails() {return memberEmails;}
        public void setMemberEmails(List<String> memberEmails) {this.memberEmails = memberEmails;}
    }
    
    public static class AddMemberRequest {
        private String email;
        public AddMemberRequest() {}
        public String getEmail() {return email;}
        public void setEmail(String email) {this.email = email;}
    }
}
