package com.rensights.admin.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.beans.factory.annotation.Value;

import com.rensights.admin.model.Deal;
import com.rensights.admin.model.DealTranslation;
import com.rensights.admin.model.ListedDeal;
import com.rensights.admin.model.RecentSale;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.rensights.admin.repository",
    includeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.rensights.admin.repository.DealRepository.class}),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.rensights.admin.repository.DealTranslationRepository.class}),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.rensights.admin.repository.ListedDealRepository.class}),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.rensights.admin.repository.RecentSaleRepository.class})
    },
    entityManagerFactoryRef = "publicEntityManagerFactory",
    transactionManagerRef = "publicTransactionManager"
)
public class PublicDataSourceConfig {

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Bean(name = "publicDataSourceProperties")
    @ConfigurationProperties("spring.public-datasource")
    public DataSourceProperties publicDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "publicDataSource")
    public DataSource publicDataSource(@Qualifier("publicDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean(name = "publicEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean publicEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("publicDataSource") DataSource dataSource) {
        Map<String, String> properties = new HashMap<>();
        // Enable table creation/update for public database (public_db_dev) where Deal, ListedDeal, and RecentSale are stored
        properties.put("hibernate.hbm2ddl.auto", "update");
        
        // CRITICAL: Only scan for specific entities to prevent creating non-deal tables in public_db_dev
        properties.put("hibernate.archive.autodetection", "class, hbm");
        
        // SECURITY FIX: Only enable SQL logging in dev profile to prevent sensitive data exposure in production
        boolean isDev = activeProfile != null && activeProfile.contains("dev");
        properties.put("hibernate.format_sql", isDev ? "true" : "false");
        properties.put("hibernate.show_sql", isDev ? "true" : "false");
        
        // Public datasource configuration - for Deal entities from public database
        // CRITICAL: Use builder with packages() but filter to only include deal-related entities
        LocalContainerEntityManagerFactoryBean factory = builder
            .dataSource(dataSource)
            .packages(Deal.class.getPackage().getName())  // Scan the package to load classes
            .persistenceUnit("public")
            .properties(properties)
            .build();
        
        // CRITICAL: Filter to only include deal-related entities (removes all non-deal entities)
        factory.setPersistenceUnitPostProcessors((PersistenceUnitPostProcessor) persistenceUnitInfo -> {
            MutablePersistenceUnitInfo unit = (MutablePersistenceUnitInfo) persistenceUnitInfo;
            String dealName = Deal.class.getName();
            String dealTranslationName = DealTranslation.class.getName();
            String listedDealName = ListedDeal.class.getName();
            String recentSaleName = RecentSale.class.getName();
            
            // Remove all entities except deal-related ones
            unit.getManagedClassNames().removeIf(className -> {
                return !className.equals(dealName) &&
                       !className.equals(dealTranslationName) &&
                       !className.equals(listedDealName) &&
                       !className.equals(recentSaleName);
            });
        });
        
        return factory;
    }

    @Bean(name = "publicTransactionManager")
    public PlatformTransactionManager publicTransactionManager(
            @Qualifier("publicEntityManagerFactory") LocalContainerEntityManagerFactoryBean publicEntityManagerFactory) {
        return new JpaTransactionManager(publicEntityManagerFactory.getObject());
    }
}

