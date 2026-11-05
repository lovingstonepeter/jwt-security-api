package com.example.jwtsecurity.controller;

import com.example.jwtsecurity.dto.ChangePasswordRequest;
import com.example.jwtsecurity.dto.UserResponse;
import com.example.jwtsecurity.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** ADMIN only (enforced in SecurityConfig). */
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    /** Any authenticated user: returns the caller's own profile. */
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.getByUsername(authentication.getName());
    }

    /** Any authenticated user: change their own password. */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(authentication.getName(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    /** ADMIN only (enforced in SecurityConfig). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
