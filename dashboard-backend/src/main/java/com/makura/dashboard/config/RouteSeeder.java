package com.makura.dashboard.config;

import com.makura.dashboard.runtime.model.Route;
import com.makura.dashboard.runtime.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds sample route configurations on startup
 * Routes are written directly to makura_runtime database
 */
@Slf4j
@Component
@Order(2) // Run after DataInitializer
@RequiredArgsConstructor
public class RouteSeeder implements CommandLineRunner {

    private final RouteRepository routeRepository;
    
    // Note: Routes are CONFIGURATIONS for the runtime service to read
    // They don't need endpoints unless forwarding to downstream systems

    @Override
    @Transactional(transactionManager = "runtimeTransactionManager")
    public void run(String... args) {
        if (routeRepository.count() > 0) {
            log.info("Routes already exist in runtime database, skipping seeding");
            return;
        }

        log.info("=== Seeding Sample Routes to Runtime Database ===");
        
        createPaymentRoutes();
        createAccountRoutes();
        createSecuritiesRoutes();
        
        log.info("=== Seeded {} sample routes to runtime database ===", routeRepository.count());
    }

    private void createPaymentRoutes() {
        // 1. Customer Credit Transfer (pacs.008)
        Route route1 = Route.builder()
                .routeId("CREDIT_TRANSFER_PACS008")
                .mode(Route.RouteMode.PASSIVE)
                .inboundFormat(Route.InboundFormat.JSON)
                .outboundFormat(Route.OutboundFormat.ISO_XML)
                .encryptionType(Route.EncryptionType.NONE)
                .yamlProfilePath("CREDIT_TRANSFER_PACS008.yaml")
                .active(true)
                .build();
        routeRepository.save(route1);
        log.info("✓ Created route: {}", route1.getRouteId());

        // 2. Payment Status Report (pacs.002)
        Route route2 = Route.builder()
                .routeId("PAYMENT_STATUS_PACS002")
                .mode(Route.RouteMode.PASSIVE)
                .inboundFormat(Route.InboundFormat.JSON)
                .outboundFormat(Route.OutboundFormat.ISO_XML)
                .encryptionType(Route.EncryptionType.NONE)
                .yamlProfilePath("PAYMENT_STATUS_PACS002.yaml")
                .active(true)
                .build();
        routeRepository.save(route2);
        log.info("✓ Created route: {}", route2.getRouteId());

        // 3. Direct Debit (pain.008)
        Route route3 = Route.builder()
                .routeId("DIRECT_DEBIT_PAIN008")
                .mode(Route.RouteMode.PASSIVE)
                .inboundFormat(Route.InboundFormat.JSON)
                .outboundFormat(Route.OutboundFormat.ISO_XML)
                .encryptionType(Route.EncryptionType.NONE)
                .yamlProfilePath("DIRECT_DEBIT_PAIN008.yaml")
                .active(true)
                .build();
        routeRepository.save(route3);
        log.info("✓ Created route: {}", route3.getRouteId());
    }

    private void createAccountRoutes() {
        // 4. Account Statement (camt.053)
        Route route4 = Route.builder()
                .routeId("ACCOUNT_STATEMENT_CAMT053")
                .mode(Route.RouteMode.PASSIVE)
                .inboundFormat(Route.InboundFormat.JSON)
                .outboundFormat(Route.OutboundFormat.ISO_XML)
                .encryptionType(Route.EncryptionType.NONE)
                .yamlProfilePath("ACCOUNT_STATEMENT_CAMT053.yaml")
                .active(true)
                .build();
        routeRepository.save(route4);
        log.info("✓ Created route: {}", route4.getRouteId());

        // 5. Balance Report (camt.052) - with encryption example
        Route route5 = Route.builder()
                .routeId("BALANCE_REPORT_CAMT052")
                .mode(Route.RouteMode.PASSIVE)
                .inboundFormat(Route.InboundFormat.JSON)
                .outboundFormat(Route.OutboundFormat.ISO_XML)
                .encryptionType(Route.EncryptionType.AES)
                .encryptionKeyRef("balance-key-001")
                .yamlProfilePath("BALANCE_REPORT_CAMT052.yaml")
                .active(true)
                .build();
        routeRepository.save(route5);
        log.info("✓ Created route: {}", route5.getRouteId());
    }

    private void createSecuritiesRoutes() {
        // 6. Securities Settlement (sese.023) - with downstream forwarding example
        Route route6 = Route.builder()
                .routeId("SECURITIES_SETTLEMENT_SESE023")
                .mode(Route.RouteMode.ACTIVE) // ACTIVE = forward to downstream after translation
                .inboundFormat(Route.InboundFormat.XML)
                .outboundFormat(Route.OutboundFormat.ISO_XML)
                .endpoint("https://securities-clearinghouse.example.com/api/settlement") // Downstream system
                .encryptionType(Route.EncryptionType.PGP)
                .encryptionKeyRef("securities-pgp-key")
                .yamlProfilePath("SECURITIES_SETTLEMENT_SESE023.yaml")
                .active(false)
                .build();
        routeRepository.save(route6);
        log.info("✓ Created route: {}", route6.getRouteId());

        // 7. SOAP to ISO Bridge
        Route route7 = Route.builder()
                .routeId("LEGACY_SOAP_BRIDGE")
                .mode(Route.RouteMode.PASSIVE)
                .inboundFormat(Route.InboundFormat.SOAP)
                .outboundFormat(Route.OutboundFormat.ISO_XML)
                .encryptionType(Route.EncryptionType.NONE)
                .yamlProfilePath("LEGACY_SOAP_BRIDGE.yaml")
                .active(true)
                .build();
        routeRepository.save(route7);
        log.info("✓ Created route: {}", route7.getRouteId());

        // 8. Demo/Test Route
        Route route8 = Route.builder()
                .routeId("DEMO_TEST_ROUTE")
                .mode(Route.RouteMode.PASSIVE)
                .inboundFormat(Route.InboundFormat.JSON)
                .outboundFormat(Route.OutboundFormat.JSON)
                .encryptionType(Route.EncryptionType.NONE)
                .yamlProfilePath("DEMO_TEST_ROUTE.yaml")
                .active(true)
                .build();
        routeRepository.save(route8);
        log.info("✓ Created route: {}", route8.getRouteId());
    }
}

