package com.makura.dashboard.controller;

import com.makura.dashboard.dto.RoleDTO;
import com.makura.dashboard.model.Permission;
import com.makura.dashboard.model.Role;
import com.makura.dashboard.repository.PermissionRepository;
import com.makura.dashboard.repository.RoleRepository;
import com.makura.dashboard.repository.UserRepository;
import com.makura.dashboard.security.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role management endpoints")
public class RoleController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @GetMapping
    @RequiresPermission("roles:view")
    @Operation(summary = "Get all roles", description = "Retrieve all role configurations")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleDTO> roleDTOs = roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roleDTOs);
    }

    @GetMapping("/{id}")
    @RequiresPermission("roles:view")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return ResponseEntity.ok(convertToDTO(role));
    }

    @PostMapping
    @RequiresPermission("roles:create")
    @Operation(summary = "Create new role")
    public ResponseEntity<RoleDTO> createRole(@RequestBody CreateRoleRequest request) {
        // Fetch permissions
        Set<Permission> permissions = new HashSet<>();
        for (String permName : request.getPermissions()) {
            permissionRepository.findByName(permName)
                    .ifPresent(permissions::add);
        }

        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .permissions(permissions)
                .isSystemRole(false)
                .build();

        Role savedRole = roleRepository.save(role);
        log.info("Created new role: {}", savedRole.getName());
        return ResponseEntity.ok(convertToDTO(savedRole));
    }

    @PutMapping("/{id}")
    @RequiresPermission("roles:update")
    @Operation(summary = "Update role")
    public ResponseEntity<RoleDTO> updateRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request) {
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (role.getIsSystemRole()) {
            throw new RuntimeException("Cannot modify system roles");
        }

        // Update permissions
        Set<Permission> permissions = new HashSet<>();
        for (String permName : request.getPermissions()) {
            permissionRepository.findByName(permName)
                    .ifPresent(permissions::add);
        }

        role.setDescription(request.getDescription());
        role.setPermissions(permissions);

        Role updatedRole = roleRepository.save(role);
        log.info("Updated role: {}", updatedRole.getName());
        return ResponseEntity.ok(convertToDTO(updatedRole));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("roles:delete")
    @Operation(summary = "Delete role")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (role.getIsSystemRole()) {
            throw new RuntimeException("Cannot delete system roles");
        }

        roleRepository.delete(role);
        log.info("Deleted role: {}", role.getName());
        return ResponseEntity.noContent().build();
    }

    private RoleDTO convertToDTO(Role role) {
        List<String> permissionNames = role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toList());

        Long userCount = userRepository.countByRolesContaining(role);

        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(permissionNames)
                .systemRole(role.getIsSystemRole())
                .userCount(userCount.intValue())
                .build();
    }

    // Inner classes for requests
    @lombok.Data
    public static class CreateRoleRequest {
        private String name;
        private String description;
        private List<String> permissions;
    }

    @lombok.Data
    public static class UpdateRoleRequest {
        private String description;
        private List<String> permissions;
    }
}



