package com.rensights.admin.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableJpaRepositories(
    basePackageClasses = {
        com.rensights.admin.repository.DeviceRepository.class,
        com.rensights.admin.repository.SubscriptionRepository.class
    },
    entityManagerFactoryRef = "backendEntityManagerFactory",
    transactionManagerRef = "backendTransactionManager"
)
public class BackendDataSourceConfig {

    @Bean(name = "backendDataSource")
    @ConfigurationProperties("spring.datasource.backend")
    public DataSource backendDataSource() {
        return DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean(name = "backendEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean backendEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("backendDataSource") DataSource dataSource) {
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.show_sql", "true");
        
        return builder
            .dataSource(dataSource)
            .packages("com.rensights.admin.model.Device", "com.rensights.admin.model.Subscription")
            .persistenceUnit("backend")
            .properties(properties)
            .build();
    }

    @Bean(name = "backendTransactionManager")
    public PlatformTransactionManager backendTransactionManager(
            @Qualifier("backendEntityManagerFactory") LocalContainerEntityManagerFactoryBean backendEntityManagerFactory) {
        return new JpaTransactionManager(backendEntityManagerFactory.getObject());
    }
}

