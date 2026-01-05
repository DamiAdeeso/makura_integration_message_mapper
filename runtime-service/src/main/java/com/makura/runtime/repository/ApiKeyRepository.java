package com.makura.runtime.repository;

import com.makura.runtime.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    @Query("SELECT ak FROM ApiKey ak WHERE ak.routeId = :routeId " +
           "AND ak.keyHash = :keyHash " +
           "AND ak.active = true " +
           "AND ak.validFrom <= :now " +
           "AND ak.validUntil >= :now")
    Optional<ApiKey> findValidKey(@Param("routeId") String routeId,
                                   @Param("keyHash") String keyHash,
                                   @Param("now") LocalDateTime now);
    
    Optional<ApiKey> findByRouteId(String routeId);
}




