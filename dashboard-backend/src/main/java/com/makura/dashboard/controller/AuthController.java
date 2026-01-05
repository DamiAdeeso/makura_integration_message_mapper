package com.makura.dashboard.controller;

import com.makura.dashboard.dto.LoginRequest;
import com.makura.dashboard.dto.LoginResponse;
import com.makura.dashboard.dto.UserDTO;
import com.makura.dashboard.model.User;
import com.makura.dashboard.repository.UserRepository;
import com.makura.dashboard.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication controller
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and authorization")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and get JWT token with permissions")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user", description = "Get current authenticated user with their permissions")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

        Set<String> permissionNames = user.getAllPermissions().stream()
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());

        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roleNames)
                .permissions(permissionNames)
                .active(user.getActive())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/permissions")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get user permissions", description = "Get list of permissions for current user")
    public ResponseEntity<Map<String, Object>> getMyPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> permissionNames = user.getAllPermissions().stream()
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());

        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("roles", roleNames);
        response.put("permissions", permissionNames);

        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("Authentication error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
}
