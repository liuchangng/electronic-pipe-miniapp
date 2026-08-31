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
 * 视频实体类
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("video")
public class Video {

    /** 视频ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 标题 */
    @Column("title")
    private String title;

    /** 描述 */
    private String description;

    /** 关联曲谱ID */
    private Long songId;

    /** 视频URL（M3U8） */
    private String videoUrl;

    /** 缩略图URL */
    private String thumbnailUrl;

    /** 时长（秒） */
    private Integer duration;

    /** 转码状态: pending/processing/done/failed */
    private String transcodeStatus;

    /** 转码失败原因 */
    private String transcodeError;

    /** 排序 */
    private Integer sortOrder;

    /** 播放次数 */
    private Integer playCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 收藏数 */
    private Integer favoriteCount;

    /** 评论数 */
    private Integer commentCount;

    /** 分享数 */
    private Integer shareCount;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 是否删除 */
    @Column(isLogicDelete = true)
    private Integer deleted;

    /** 关联曲谱名称（非数据库字段） */
    @Column(ignore = true)
    private String songTitle;
}
