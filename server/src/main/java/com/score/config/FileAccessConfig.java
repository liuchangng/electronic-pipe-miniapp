package com.score.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件访问配置
 * 将 /uploads/** 请求映射到本地 uploads 目录，提供上传文件的 HTTP 访问
 * 替代 x-file-storage 内置的 enable-access（该功能在某些版本下不可靠）
 *
 * @author Score Team
 * @since 1.0.0
 */
@Configuration
public class FileAccessConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(FileAccessConfig.class);

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 解析为绝对路径
        Path path = Paths.get(uploadPath).toAbsolutePath().normalize();

        // 如果目录不存在则创建
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                log.info("Created uploads directory: {}", path);
            } catch (Exception e) {
                log.warn("Failed to create uploads directory: {}", path, e);
            }
        }

        // 注册资源处理器：/uploads/** -> file:<absolute-path>/
        String location = "file:" + path.toString() + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);

        log.info("File access mapped: /uploads/** -> {}", location);
    }
}