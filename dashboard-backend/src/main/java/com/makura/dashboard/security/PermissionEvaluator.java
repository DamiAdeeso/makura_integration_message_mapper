package com.makura.dashboard.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Custom permission evaluator for fine-grained access control
 * Uses JWT claims (permissions) - no database queries needed!
 */
@Slf4j
@Component("permissionEvaluator")
public class PermissionEvaluator {

    /**
     * Check if current user has a specific permission
     * Permissions are extracted from JWT claims (stateless, no DB query)
     */
    public boolean hasPermission(Authentication authentication, String permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Get authorities from authentication (already loaded from JWT claims)
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        boolean hasPermission = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(permission));
        
        log.debug("User {} {} permission {} (from JWT claims)", 
                authentication.getName(), 
                hasPermission ? "HAS" : "LACKS", 
                permission);
        
        return hasPermission;
    }

    /**
     * Check if current user has any of the specified permissions
     */
    public boolean hasAnyPermission(Authentication authentication, String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(authentication, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user has all of the specified permissions
     */
    public boolean hasAllPermissions(Authentication authentication, String... permissions) {
        for (String permission : permissions) {
            if (!hasPermission(authentication, permission)) {
                return false;
            }
        }
        return true;
    }
}

