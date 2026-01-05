package com.makura.dashboard.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect to enforce permission-based access control
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionEvaluator permissionEvaluator;

    @Around("@annotation(com.makura.dashboard.security.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String[] requiredPermissions = annotation.value();
        boolean requireAll = annotation.requireAll();

        boolean hasAccess;
        if (requireAll) {
            hasAccess = permissionEvaluator.hasAllPermissions(authentication, requiredPermissions);
        } else {
            hasAccess = permissionEvaluator.hasAnyPermission(authentication, requiredPermissions);
        }

        if (!hasAccess) {
            String permissionList = String.join(", ", requiredPermissions);
            log.warn("Access denied for user {} to method {} - Required permissions: {}",
                    authentication.getName(), method.getName(), permissionList);
            throw new AccessDeniedException("Insufficient permissions: " + permissionList);
        }

        return joinPoint.proceed();
    }
}



