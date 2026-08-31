package com.score.controller;

import com.score.common.R;
import com.score.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统配置公开接口（无需登录）
 *
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private SystemConfigService configService;

    /**
     * 获取公开的系统配置（系统名称、版本、Logo等）
     */
    @GetMapping("/public")
    public R getPublicConfig() {
        Map<String, String> allConfig = configService.getAllConfigMap();
        // 只返回公开配置，过滤敏感信息
        Map<String, String> publicConfig = new java.util.HashMap<>();
        String[] publicKeys = {"system_name", "system_version", "system_logo", "system_subtitle"};
        for (String key : publicKeys) {
            if (allConfig.containsKey(key)) {
                publicConfig.put(key, allConfig.get(key));
            }
        }
        return R.ok(publicConfig);
    }
}