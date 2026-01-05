package com.makura.dashboard.config;

import com.makura.dashboard.model.Permission;
import com.makura.dashboard.model.Role;
import com.makura.dashboard.model.User;
import com.makura.dashboard.repository.PermissionRepository;
import com.makura.dashboard.repository.RoleRepository;
import com.makura.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Initialize default permissions, roles, and users on startup
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("=== Initializing Permissions and Roles ===");
        
        // Create permissions
        createPermissions();
        
        // Create default roles
        createRoles();
        
        // Create default users
        createUsers();
        
        log.info("=== Data initialization complete ===");
    }

    private void createPermissions() {
        String[][] permissionsData = {
            // Routes permissions
            {"routes:view", "routes", "view", "View route configurations"},
            {"routes:create", "routes", "create", "Create new routes"},
            {"routes:update", "routes", "update", "Update existing routes"},
            {"routes:delete", "routes", "delete", "Delete routes"},
            {"routes:toggle", "routes", "toggle", "Enable/disable routes"},
            
            // Mappings permissions
            {"mappings:view", "mappings", "view", "View mapping configurations"},
            {"mappings:create", "mappings", "create", "Create new mappings"},
            {"mappings:update", "mappings", "update", "Update existing mappings"},
            {"mappings:delete", "mappings", "delete", "Delete mappings"},
            {"mappings:generate", "mappings", "generate", "Generate YAML files"},
            
            // Users permissions
            {"users:view", "users", "view", "View users"},
            {"users:create", "users", "create", "Create new users"},
            {"users:update", "users", "update", "Update existing users"},
            {"users:delete", "users", "delete", "Delete users"},
            {"users:manage", "users", "manage", "Full user management"},
            
            // Roles permissions
            {"roles:view", "roles", "view", "View roles"},
            {"roles:create", "roles", "create", "Create new roles"},
            {"roles:update", "roles", "update", "Update existing roles"},
            {"roles:delete", "roles", "delete", "Delete roles"},
            {"roles:manage", "roles", "manage", "Full role management"},
            
            // Metrics permissions
            {"metrics:view", "metrics", "view", "View metrics and statistics"},
            {"metrics:export", "metrics", "export", "Export metrics data"},
            
            // API Keys permissions
            {"api-keys:view", "api-keys", "view", "View API keys"},
            {"api-keys:create", "api-keys", "create", "Create new API keys"},
            {"api-keys:update", "api-keys", "update", "Update existing API keys"},
            {"api-keys:delete", "api-keys", "delete", "Delete API keys"},
            
            // System permissions
            {"system:health", "system", "health", "View system health status"},
            {"system:config", "system", "config", "View/edit system configuration"},
        };

        for (String[] permData : permissionsData) {
            if (!permissionRepository.existsByName(permData[0])) {
                Permission permission = Permission.builder()
                        .name(permData[0])
                        .resource(permData[1])
                        .action(permData[2])
                        .description(permData[3])
                        .build();
                permissionRepository.save(permission);
            }
        }
        
        log.info("✓ Initialized {} permissions", permissionsData.length);
    }

    private void createRoles() {
        // ADMIN Role - Full access
        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .description("Full system access - all permissions")
                    .isSystemRole(true)
                    .permissions(new HashSet<>(permissionRepository.findAll()))
                    .build();
            roleRepository.save(adminRole);
            log.info("✓ Created ADMIN role with all permissions");
        }

        // OPERATOR Role - Can manage routes and mappings
        if (!roleRepository.existsByName("OPERATOR")) {
            Set<Permission> operatorPerms = new HashSet<>();
            addPermissionByName(operatorPerms, "routes:view", "routes:create", "routes:update", "routes:toggle");
            addPermissionByName(operatorPerms, "mappings:view", "mappings:create", "mappings:update", "mappings:generate");
            addPermissionByName(operatorPerms, "api-keys:view", "api-keys:create", "api-keys:update");
            addPermissionByName(operatorPerms, "metrics:view", "system:health");
            
            Role operatorRole = Role.builder()
                    .name("OPERATOR")
                    .description("Can manage routes and mappings")
                    .isSystemRole(true)
                    .permissions(operatorPerms)
                    .build();
            roleRepository.save(operatorRole);
            log.info("✓ Created OPERATOR role");
        }

        // VIEWER Role - Read-only access
        if (!roleRepository.existsByName("VIEWER")) {
            Set<Permission> viewerPerms = new HashSet<>();
            addPermissionByName(viewerPerms, "routes:view", "mappings:view", "metrics:view", "system:health");
            
            Role viewerRole = Role.builder()
                    .name("VIEWER")
                    .description("Read-only access")
                    .isSystemRole(true)
                    .permissions(viewerPerms)
                    .build();
            roleRepository.save(viewerRole);
            log.info("✓ Created VIEWER role");
        }
    }

    private void addPermissionByName(Set<Permission> permissions, String... permissionNames) {
        for (String name : permissionNames) {
            permissionRepository.findByName(name).ifPresent(permissions::add);
        }
    }

    private void createUsers() {
        // Create default admin user
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@makura.com")
                    .fullName("System Administrator")
                    .roles(adminRoles)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info("✓ Created admin user (username: admin, password: admin123)");
        }

        // Create operator user
        if (!userRepository.existsByUsername("operator")) {
            Role operatorRole = roleRepository.findByName("OPERATOR").orElseThrow();
            Set<Role> operatorRoles = new HashSet<>();
            operatorRoles.add(operatorRole);
            
            User operator = User.builder()
                    .username("operator")
                    .password(passwordEncoder.encode("operator123"))
                    .email("operator@makura.com")
                    .fullName("System Operator")
                    .roles(operatorRoles)
                    .active(true)
                    .build();
            userRepository.save(operator);
            log.info("✓ Created operator user (username: operator, password: operator123)");
        }

        // Create viewer user
        if (!userRepository.existsByUsername("viewer")) {
            Role viewerRole = roleRepository.findByName("VIEWER").orElseThrow();
            Set<Role> viewerRoles = new HashSet<>();
            viewerRoles.add(viewerRole);
            
            User viewer = User.builder()
                    .username("viewer")
                    .password(passwordEncoder.encode("viewer123"))
                    .email("viewer@makura.com")
                    .fullName("System Viewer")
                    .roles(viewerRoles)
                    .active(true)
                    .build();
            userRepository.save(viewer);
            log.info("✓ Created viewer user (username: viewer, password: viewer123)");
        }
    }
}


