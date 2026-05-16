package com.flashcart.service;

import com.flashcart.entities.Group;
import com.flashcart.entities.GroupType;
import com.flashcart.entities.User;
import com.flashcart.repository.GroupRepository;
import com.flashcart.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GroupService {
    
    private static final Logger log = LoggerFactory.getLogger(GroupService.class);
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public GroupService(GroupRepository groupRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Transactional
    public Group createGroup(String name, String description, GroupType groupType, User createdBy, List<String> memberEmails) {
        Set<User> members = new HashSet<>();
        members.add(createdBy);
        
        for (String email : memberEmails) {
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        log.info("Creating new user for email: {}", email);
                        return userRepository.save(User.builder()
                                .email(email)
                                .username(email.split("@")[0])
                                .password(passwordEncoder.encode("temp_password"))
                                .isActive(true)
                                .build());
                    });
            members.add(user);
        }
        
        Group group = Group.builder()
                .name(name).description(description).groupType(groupType)
                .createdBy(createdBy).members(members).isActive(true).build();
        
        group = groupRepository.save(group);
        log.info("Created new group: {} with {} members", group.getId(), members.size());
        return group;
    }
    
    @Transactional(readOnly = true)
    public List<Group> getUserGroups(Long userId) {
        return groupRepository.findGroupsByMember(userId);
    }
    
    @Transactional(readOnly = true)
    public Group getGroupById(Long groupId) {
        return groupRepository.findByIdAndIsActiveTrue(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }
    
    @Transactional
    public Group addMemberToGroup(Long groupId, String userEmail) {
        Group group = getGroupById(groupId);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        group.getMembers().add(user);
        return groupRepository.save(group);
    }
    
    @Transactional
    public Group removeMemberFromGroup(Long groupId, Long userId) {
        Group group = getGroupById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        group.getMembers().remove(user);
        return groupRepository.save(group);
    }
}
