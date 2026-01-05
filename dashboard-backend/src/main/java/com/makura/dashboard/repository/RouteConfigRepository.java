package com.makura.dashboard.repository;

import com.makura.dashboard.model.RouteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteConfigRepository extends JpaRepository<RouteConfig, Long> {
    Optional<RouteConfig> findByRouteId(String routeId);
    List<RouteConfig> findByActive(Boolean active);
    List<RouteConfig> findByPublished(Boolean published);
    boolean existsByRouteId(String routeId);
}




