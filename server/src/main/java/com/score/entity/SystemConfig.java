package com.score.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统配置实体
 *
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("system_config")
public class SystemConfig {

    @Id(keyType = KeyType.Auto)
    private Long id;

    @Column("config_key")
    private String configKey;

    @Column("config_value")
    private String configValue;

    private String description;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}