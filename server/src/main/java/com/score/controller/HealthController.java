package com.score.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查接口
 * 
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", "电吹管曲谱库后端服务运行正常");
        return result;
    }
}
