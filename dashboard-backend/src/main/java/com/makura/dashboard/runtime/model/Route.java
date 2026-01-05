package com.makura.dashboard.runtime.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Route entity for runtime database (makura_runtime)
 * This is the same structure as runtime-service Route entity
 */
@Entity
@Table(name = "routes", indexes = {
    @Index(name = "idx_route_id", columnList = "route_id"),
    @Index(name = "idx_active", columnList = "active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_id", nullable = false, unique = true, length = 100)
    private String routeId;

    @Column(name = "inbound_format", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private InboundFormat inboundFormat;

    @Column(name = "outbound_format", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private OutboundFormat outboundFormat;

    @Column(name = "mode", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RouteMode mode;

    @Column(name = "endpoint", length = 500)
    private String endpoint;

    @Column(name = "encryption_type", length = 50)
    @Enumerated(EnumType.STRING)
    private EncryptionType encryptionType;

    @Column(name = "encryption_key_ref", length = 200)
    private String encryptionKeyRef;

    @Column(name = "yaml_profile_path", length = 100)
    private String yamlProfilePath;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum InboundFormat {
        JSON, SOAP, XML, PROPRIETARY_XML
    }

    public enum OutboundFormat {
        ISO_XML, JSON, XML
    }

    public enum RouteMode {
        ACTIVE, PASSIVE
    }

    public enum EncryptionType {
        NONE, AES, PGP
    }
}



