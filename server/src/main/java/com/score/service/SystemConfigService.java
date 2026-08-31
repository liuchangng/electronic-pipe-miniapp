package com.score.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.score.entity.SystemConfig;
import com.score.entity.table.SystemConfigTableDef;
import com.score.mapper.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务
 *
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class SystemConfigService {

    private static final SystemConfigTableDef CONFIG = SystemConfigTableDef.SYSTEM_CONFIG;

    @Autowired
    private SystemConfigMapper configMapper;

    /**
     * 获取所有配置（以Map形式返回）
     */
    public Map<String, String> getAllConfigMap() {
        List<SystemConfig> list = configMapper.selectListByQuery(QueryWrapper.create());
        Map<String, String> map = new HashMap<>();
        for (SystemConfig c : list) {
            map.put(c.getConfigKey(), c.getConfigValue());
        }
        return map;
    }

    /**
     * 获取单个配置值
     */
    public String getConfigValue(String key) {
        SystemConfig config = configMapper.selectOneByQuery(
            QueryWrapper.create().where(CONFIG.CONFIG_KEY.eq(key))
        );
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 设置配置值（存在则更新，不存在则插入）
     */
    public void setConfigValue(String key, String value, String description) {
        SystemConfig existing = configMapper.selectOneByQuery(
            QueryWrapper.create().where(CONFIG.CONFIG_KEY.eq(key))
        );
        if (existing != null) {
            existing.setConfigValue(value);
            existing.setUpdatedAt(LocalDateTime.now());
            if (description != null) {
                existing.setDescription(description);
            }
            configMapper.update(existing);
        } else {
            SystemConfig config = SystemConfig.builder()
                .configKey(key)
                .configValue(value)
                .description(description)
                .updatedAt(LocalDateTime.now())
                .build();
            configMapper.insert(config);
        }
    }

    /**
     * 批量设置配置
     */
    public void batchSetConfig(Map<String, String> configMap) {
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            setConfigValue(entry.getKey(), entry.getValue(), null);
        }
    }
}