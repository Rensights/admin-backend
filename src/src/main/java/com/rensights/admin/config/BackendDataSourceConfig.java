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

import com.rensights.admin.model.AnalysisRequest;
import com.rensights.admin.model.Deal;
import com.rensights.admin.model.Device;
import com.rensights.admin.model.Subscription;
import com.rensights.admin.model.User;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.rensights.admin.repository",
    includeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.rensights.admin.repository.AnalysisRequestRepository.class}),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.rensights.admin.repository.DealRepository.class}),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.rensights.admin.repository.DeviceRepository.class}),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.rensights.admin.repository.SubscriptionRepository.class}),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.rensights.admin.repository.UserRepository.class})
    },
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.rensights.admin.repository.AdminUserRepository.class})
    },
    entityManagerFactoryRef = "backendEntityManagerFactory",
    transactionManagerRef = "backendTransactionManager"
)
public class BackendDataSourceConfig {

    @Bean(name = "backendDataSourceProperties")
    @ConfigurationProperties("spring.public-datasource")
    public DataSourceProperties backendDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "backendDataSource")
    public DataSource backendDataSource(@Qualifier("backendDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean(name = "backendEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean backendEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("backendDataSource") DataSource dataSource) {
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.show_sql", "true");
        
        return builder
            .dataSource(dataSource)
            .packages(AnalysisRequest.class, Deal.class, Device.class, Subscription.class, User.class)
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

