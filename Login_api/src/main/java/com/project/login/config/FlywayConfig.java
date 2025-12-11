package com.project.login.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Value("${spring.flyway.schemas}")
    private String schemas;

    @Value("${spring.flyway.locations}")
    private String locations;

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemas)
                .locations(locations)
                .baselineOnMigrate(true)  // ✅ 对已有非空 schema 自动创建基线
                .baselineVersion("1")     // 可以自定义 baseline 版本
                .load();

        // 如果需要，可以先检查是否 baseline
        flyway.baseline();

        return flyway;
    }
}

