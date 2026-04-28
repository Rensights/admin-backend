package com.rensights.admin.config;

import com.rensights.admin.model.Subscription;
import com.rensights.admin.model.User;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.rensights.admin.repository.publicdb",
    entityManagerFactoryRef = "publicEntityManagerFactory",
    transactionManagerRef = "publicTransactionManager"
)
public class PublicDataSourceConfig {

    @Bean(name = "publicDataSourceProperties")
    @ConfigurationProperties("app.datasource.public")
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
        return builder
            .dataSource(dataSource)
            .packages(User.class, Subscription.class)
            .persistenceUnit("public")
            .properties(Map.of("hibernate.hbm2ddl.auto", "none"))
            .build();
    }

    @Bean(name = "publicTransactionManager")
    public PlatformTransactionManager publicTransactionManager(
            @Qualifier("publicEntityManagerFactory") LocalContainerEntityManagerFactoryBean publicEntityManagerFactory) {
        return new JpaTransactionManager(publicEntityManagerFactory.getObject());
    }
}
