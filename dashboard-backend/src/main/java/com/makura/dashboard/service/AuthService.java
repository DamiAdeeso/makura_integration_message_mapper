package com.makura.dashboard.service;

import com.makura.dashboard.dto.LoginRequest;
import com.makura.dashboard.dto.LoginResponse;
import com.makura.dashboard.dto.UserDTO;
import com.makura.dashboard.model.User;
import com.makura.dashboard.repository.UserRepository;
import com.makura.dashboard.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Authentication service for login and JWT generation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Load user details
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update last login time
            user.setLastLogin(java.time.LocalDateTime.now());
            userRepository.save(user);

            // Get all permissions for JWT (permission-based, not role-based)
            java.util.Set<String> permissionNames = user.getAllPermissions().stream()
                    .map(permission -> permission.getName())
                    .collect(java.util.stream.Collectors.toSet());

            // Generate JWT token with permissions in claims
            String token = jwtUtil.generateToken(
                    user.getUsername(), 
                    new java.util.ArrayList<>(permissionNames)
            );

            // Build response with roles and permissions
            java.util.List<String> roleNames = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(java.util.stream.Collectors.toList());

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

            log.info("User logged in successfully: {}", user.getUsername());

            return LoginResponse.builder()
                    .token(token)
                    .user(userDTO)
                    .build();

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", request.getUsername());
            throw new RuntimeException("Invalid username or password");
        }
    }
}


