package com.score.config;

import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token JWT 配置
 * 使用 Stateless 模式：纯JWT无状态，token自包含
 * 重启后 token 仍然有效，不依赖服务端存储
 *
 * @author Score Team
 * @since 1.0.0
 */
@Configuration
public class SaTokenJwtConfig {

    /**
     * Sa-Token 整合 JWT 的 Stateless 模式
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForStateless();
    }
}