package com.score;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.dromara.x.file.storage.spring.EnableFileStorage;

/**
 * 电吹管曲谱库后端服务
 * 
 * @author Score Team
 * @since 1.0.0
 */
@EnableFileStorage
@SpringBootApplication
@MapperScan("com.score.mapper")
public class ScoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScoreApplication.class, args);
    }
}
