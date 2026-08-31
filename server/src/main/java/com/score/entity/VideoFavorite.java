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
 * 视频收藏实体类
 *
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("video_favorite")
public class VideoFavorite {

    /** 收藏ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 用户ID */
    @Column("user_id")
    private Long userId;

    /** 视频ID */
    @Column("video_id")
    private Long videoId;

    /** 状态: active/cancelled */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}