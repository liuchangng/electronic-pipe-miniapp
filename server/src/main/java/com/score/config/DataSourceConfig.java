package com.score.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * 数据源配置
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Configuration
public class DataSourceConfig {

    /**
     * 创建数据源
     */
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:score.db");
        config.setUsername("");
        config.setPassword("");
        
        return new HikariDataSource(config);
    }
}
