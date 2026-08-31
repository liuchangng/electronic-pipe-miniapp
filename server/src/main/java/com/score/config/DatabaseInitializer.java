package com.score.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 数据库初始化器
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Component
public class DatabaseInitializer {

    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * 初始化数据库表结构
     */
    @PostConstruct
    public void initDatabase() {
        if (dataSource == null) {
            System.out.println("⚠️ DataSource 未配置，跳过数据库初始化");
            return;
        }
        
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/schema.sql"));
            System.out.println("✅ 数据库表结构初始化完成");
        } catch (Exception e) {
            System.err.println("❌ 数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
