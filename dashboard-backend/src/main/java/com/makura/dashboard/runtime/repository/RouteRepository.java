package com.makura.dashboard.runtime.repository;

import com.makura.dashboard.runtime.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByRouteId(String routeId);
    Optional<Route> findByRouteIdAndActiveTrue(String routeId);
    boolean existsByRouteId(String routeId);
    List<Route> findByActive(Boolean active);
}



