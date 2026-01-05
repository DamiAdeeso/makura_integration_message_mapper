package com.makura.dashboard.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Stores field-to-field mapping definitions
 * This is the raw mapping data before YAML generation
 */
@Entity
@Table(name = "mapping_definitions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MappingDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_config_id", nullable = false)
    private Long routeConfigId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MappingDirection direction;

    @Column(name = "source_path", nullable = false, length = 500)
    private String sourcePath;

    @Column(name = "target_path", nullable = false, length = 500)
    private String targetPath;

    @Column(name = "transformation_function", length = 100)
    private String transformationFunction;

    @Column(name = "default_value", length = 255)
    private String defaultValue;

    @Column(nullable = false)
    private Boolean required = false;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum MappingDirection {
        REQUEST,  // Source → ISO
        RESPONSE  // ISO → Source
    }
}




