package com.score.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;

/**
 * Sa-Token 配置
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin()
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/user/login",
                    "/api/song/list",
                    "/api/song/page",
                    "/api/song/detail/**",
                    "/api/knowledge/list",
                    "/api/knowledge/detail/**",
                    "/api/banner/list",
                    "/api/video/list",
                    "/api/video/detail/**",
                    "/api/video/all",
                    "/api/video/page",
                    "/api/video/songs",
                    "/api/video/share/record",
                    "/api/comment/list",
                    "/api/comment/count",
                    "/api/health",
                    "/api/file/upload",
                    "/api/search/hot",
                    "/api/admin/login",
                    "/api/config/public"
                );
    }
}
