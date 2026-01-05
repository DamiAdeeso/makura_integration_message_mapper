package com.makura.dashboard.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Dual datasource configuration:
 * - Primary (dashboard): makura_dashboard - for users, roles, permissions, audit_log
 * - Secondary (runtime): makura_runtime - for routes, api_keys
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    /**
     * Primary datasource properties for dashboard
     */
    @Primary
    @Bean(name = "dashboardDataSourceProperties")
    @ConfigurationProperties("spring.datasource.dashboard")
    public DataSourceProperties dashboardDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Primary datasource for dashboard (users, roles, permissions, audit_log)
     */
    @Primary
    @Bean(name = "dashboardDataSource")
    public DataSource dashboardDataSource(@Qualifier("dashboardDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * Secondary datasource properties for runtime
     */
    @Bean(name = "runtimeDataSourceProperties")
    @ConfigurationProperties("spring.datasource.runtime")
    public DataSourceProperties runtimeDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * Secondary datasource for runtime (routes, api_keys)
     */
    @Bean(name = "runtimeDataSource")
    public DataSource runtimeDataSource(@Qualifier("runtimeDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * Primary entity manager factory for dashboard entities
     */
    @Primary
    @Bean(name = "dashboardEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean dashboardEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("dashboardDataSource") DataSource dataSource) {
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "true");
        
        return builder
                .dataSource(dataSource)
                .packages("com.makura.dashboard.model")
                .persistenceUnit("dashboard")
                .properties(properties)
                .build();
    }

    /**
     * Secondary entity manager factory for runtime entities
     */
    @Bean(name = "runtimeEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean runtimeEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("runtimeDataSource") DataSource dataSource) {
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "true");
        
        return builder
                .dataSource(dataSource)
                .packages("com.makura.dashboard.runtime.model")
                .persistenceUnit("runtime")
                .properties(properties)
                .build();
    }

    /**
     * Primary transaction manager for dashboard
     */
    @Primary
    @Bean(name = "dashboardTransactionManager")
    public PlatformTransactionManager dashboardTransactionManager(
            @Qualifier("dashboardEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    /**
     * Secondary transaction manager for runtime
     */
    @Bean(name = "runtimeTransactionManager")
    public PlatformTransactionManager runtimeTransactionManager(
            @Qualifier("runtimeEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    /**
     * Dashboard repositories configuration
     */
    @Configuration
    @EnableJpaRepositories(
            basePackages = "com.makura.dashboard.repository",
            entityManagerFactoryRef = "dashboardEntityManagerFactory",
            transactionManagerRef = "dashboardTransactionManager"
    )
    static class DashboardRepositoriesConfig {
    }

    /**
     * Runtime repositories configuration
     */
    @Configuration
    @EnableJpaRepositories(
            basePackages = "com.makura.dashboard.runtime.repository",
            entityManagerFactoryRef = "runtimeEntityManagerFactory",
            transactionManagerRef = "runtimeTransactionManager"
    )
    static class RuntimeRepositoriesConfig {
    }
}

