package com.makura.dashboard.repository;

import com.makura.dashboard.model.MappingDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MappingDefinitionRepository extends JpaRepository<MappingDefinition, Long> {
    List<MappingDefinition> findByRouteConfigIdOrderBySortOrder(Long routeConfigId);
    List<MappingDefinition> findByRouteConfigIdAndDirection(Long routeConfigId, MappingDefinition.MappingDirection direction);
    void deleteByRouteConfigId(Long routeConfigId);
}




