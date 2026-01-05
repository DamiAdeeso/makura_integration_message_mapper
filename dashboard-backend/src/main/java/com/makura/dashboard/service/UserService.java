package com.makura.dashboard.service;

import com.makura.dashboard.dto.ChangePasswordRequest;
import com.makura.dashboard.dto.CreateUserRequest;
import com.makura.dashboard.dto.UpdateUserRequest;
import com.makura.dashboard.dto.UserDTO;
import com.makura.dashboard.model.Role;
import com.makura.dashboard.model.User;
import com.makura.dashboard.repository.RoleRepository;
import com.makura.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing users
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toDTO(user);
    }

    /**
     * Get user by username
     */
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return toDTO(user);
    }

    /**
     * Create a new user
     */
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("User with username '" + request.getUsername() + "' already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with email '" + request.getEmail() + "' already exists");
        }

        // Get roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            roles = request.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId)))
                    .collect(Collectors.toSet());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .roles(roles)
                .active(request.getActive() != null ? request.getActive() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);
        log.info("Created new user: {}", saved.getUsername());
        return toDTO(saved);
    }

    /**
     * Update an existing user
     */
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Check if email is being changed to an existing email
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("User with email '" + request.getEmail() + "' already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        
        if (request.getRoleIds() != null) {
            Set<Role> roles = request.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        user.setUpdatedAt(LocalDateTime.now());

        User updated = userRepository.save(user);
        log.info("Updated user: {}", updated.getUsername());
        return toDTO(updated);
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("Changed password for user: {}", user.getUsername());
    }

    /**
     * Delete a user
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Prevent deleting the last admin user
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));
        
        if (isAdmin) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRoles().stream()
                            .anyMatch(role -> "ADMIN".equals(role.getName())))
                    .count();
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot delete the last admin user");
            }
        }

        userRepository.delete(user);
        log.info("Deleted user: {}", user.getUsername());
    }

    /**
     * Toggle user active status
     */
    @Transactional
    public UserDTO toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setActive(!user.getActive());
        user.setUpdatedAt(LocalDateTime.now());

        User updated = userRepository.save(user);
        log.info("Toggled user {} status to: {}", updated.getUsername(), updated.getActive());
        return toDTO(updated);
    }

    /**
     * Convert User entity to UserDTO (without password)
     */
    private UserDTO toDTO(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        Set<String> permissionNames = user.getAllPermissions().stream()
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());

        return UserDTO.builder()
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
    }
}

