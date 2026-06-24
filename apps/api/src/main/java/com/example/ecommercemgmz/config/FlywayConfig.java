package com.example.ecommercemgmz.config;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {
    @Bean
    Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
    }

    @Bean
    FlywayMigrationRunner flywayMigrationRunner(Flyway flyway) {
        return new FlywayMigrationRunner(flyway);
    }

    @Bean
    static BeanFactoryPostProcessor entityManagerFactoryDependsOnFlyway() {
        return beanFactory -> {
            if (beanFactory.containsBeanDefinition("entityManagerFactory")) {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition("entityManagerFactory");
                beanDefinition.setDependsOn("flywayMigrationRunner");
            }
        };
    }

    static class FlywayMigrationRunner {
        FlywayMigrationRunner(Flyway flyway) {
            flyway.migrate();
        }
    }
}
