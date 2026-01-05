package com.makura.dashboard.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Route configuration managed by dashboard
 * This mirrors/extends the Route entity from runtime-service
 */
@Entity
@Table(name = "route_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_id", unique = true, nullable = false, length = 100)
    private String routeId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "inbound_format", nullable = false, length = 50)
    private String inboundFormat;

    @Column(name = "outbound_format", nullable = false, length = 50)
    private String outboundFormat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteMode mode;

    @Column(length = 500)
    private String endpoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "encryption_type")
    private EncryptionType encryptionType;

    @Column(name = "encryption_key_ref", length = 100)
    private String encryptionKeyRef;

    @Column(name = "yaml_content", columnDefinition = "TEXT")
    private String yamlContent;

    @Column(name = "yaml_version")
    @Builder.Default
    private Integer yamlVersion = 1;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean published = false;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum RouteMode {
        ACTIVE,  // Forward to downstream system
        PASSIVE  // Return ISO without forwarding
    }

    public enum EncryptionType {
        NONE,
        AES,
        PGP
    }
}


