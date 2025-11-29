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

import com.rensights.admin.model.User;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.rensights.admin.repository",
    includeFilters = {},
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.rensights.admin.repository.AdminUserRepository.class}),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.rensights.admin.repository.UserRepository.class}),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.rensights.admin.repository.DeviceRepository.class}),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.rensights.admin.repository.SubscriptionRepository.class})
    },
    entityManagerFactoryRef = "publicEntityManagerFactory",
    transactionManagerRef = "publicTransactionManager"
)
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
        
        return builder
            .dataSource(dataSource)
            .packages()  // Public datasource no longer has entities - reserved for future use
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

