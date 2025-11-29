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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
// No JPA repositories configured for public datasource - reserved for future use
// Removed @EnableJpaRepositories since there are no repositories using this datasource
public class PublicDataSourceConfig {

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
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.show_sql", "true");
        
        // Public datasource configuration - no entities currently, reserved for future use
        // Provide a placeholder package (the config package itself) to create EntityManagerFactory
        return builder
            .dataSource(dataSource)
            .packages(PublicDataSourceConfig.class.getPackage().getName())  // Placeholder package - no entities in this package
            .persistenceUnit("public")
            .properties(properties)
            .build();
    }

    @Bean(name = "publicTransactionManager")
    public PlatformTransactionManager publicTransactionManager(
            @Qualifier("publicEntityManagerFactory") LocalContainerEntityManagerFactoryBean publicEntityManagerFactory) {
        return new JpaTransactionManager(publicEntityManagerFactory.getObject());
    }
}

