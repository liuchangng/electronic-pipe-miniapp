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
 * 视频分享记录实体类
 *
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("video_share")
public class VideoShare {

    /** 分享记录ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 用户ID（可为空，分享无需登录） */
    @Column("user_id")
    private Long userId;

    /** 视频ID */
    @Column("video_id")
    private Long videoId;

    /** 创建时间 */
    private LocalDateTime createdAt;
}