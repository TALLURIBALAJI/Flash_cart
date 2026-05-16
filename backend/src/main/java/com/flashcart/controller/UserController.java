package com.flashcart.controller;

import com.flashcart.entities.ExpenseCategory;
import com.flashcart.entities.GroupType;
import com.flashcart.entities.User;
import com.flashcart.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/users/exists/{email}")
    public ResponseEntity<?> userExists(@PathVariable String email) {
        return ResponseEntity.ok(Map.of("exists", userService.userExists(email)));
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        List<Map<String, String>> cats = new ArrayList<>();
        for (ExpenseCategory c : ExpenseCategory.values()) {
            cats.add(Map.of("name", c.name(), "icon", c.getIcon(), "label", c.getLabel()));
        }
        return ResponseEntity.ok(cats);
    }

    @GetMapping("/group-types")
    public ResponseEntity<?> getGroupTypes() {
        List<Map<String, String>> types = new ArrayList<>();
        for (GroupType t : GroupType.values()) {
            types.add(Map.of("name", t.name(), "icon", t.getIcon(), "label", t.getLabel()));
        }
        return ResponseEntity.ok(types);
    }
}
