package com.makura.dashboard.controller;

import com.makura.dashboard.dto.ChangePasswordRequest;
import com.makura.dashboard.dto.CreateUserRequest;
import com.makura.dashboard.dto.UpdateUserRequest;
import com.makura.dashboard.dto.UserDTO;
import com.makura.dashboard.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.makura.dashboard.security.RequiresPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing users
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users (Admin only)")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @RequiresPermission("users:view")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    @RequiresPermission("users:view")
    @Operation(summary = "Get user by username", description = "Retrieve a specific user by username")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user (Admin only)")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating new user: {}", request.getUsername());
        UserDTO created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user (Admin only)")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        
        log.info("Updating user with id: {}", id);
        UserDTO updated = userService.updateUser(id, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/change-password")
    @Operation(summary = "Change user password", description = "Change password for a user (Admin only)")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        
        log.info("Changing password for user with id: {}", id);
        userService.changePassword(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user (Admin only)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/toggle")
    @RequiresPermission("users:update")
    @Operation(summary = "Toggle user status", description = "Enable or disable a user")
    public ResponseEntity<UserDTO> toggleUserStatus(@PathVariable Long id) {
        log.info("Toggling status for user with id: {}", id);
        UserDTO updated = userService.toggleUserStatus(id);
        return ResponseEntity.ok(updated);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("Error in UserController: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}

