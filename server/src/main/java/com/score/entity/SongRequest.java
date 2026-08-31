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
 * 求谱申请实体类
 *
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("song_request")
public class SongRequest {

    /** 求谱ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 用户ID */
    @Column("user_id")
    private Long userId;

    /** 曲谱名称 */
    @Column("song_name")
    private String songName;

    /** 原曲作者 */
    private String artist;

    /** 分类 */
    private String category;

    /** 补充说明 */
    private String description;

    /** 状态：pending(待处理), accepted(已采纳), rejected(未采纳) */
    private String status;

    /** 管理员回复 */
    private String reply;

    /** 创建时间 */
    @Column("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @Column("updated_at")
    private LocalDateTime updatedAt;

    /** 逻辑删除 */
    private Integer deleted;
}