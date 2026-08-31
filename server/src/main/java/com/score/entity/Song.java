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
 * 曲谱实体类
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("song")
public class Song {

    /** 曲谱ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 曲谱标题 */
    @Column("title")
    private String title;

    /** 作者 */
    private String author;

    /** 图标文字 */
    private String icon;

    /** 图标颜色 */
    private String color;

    /** 描述 */
    private String description;

    /** 曲谱图片URL */
    private String imageUrl;

    /** 曲谱图片URL（高清版） */
    @Column("score_image_url")
    private String scoreImageUrl;

    /** 视频URL（M3U8） */
    private String videoUrl;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    /** 分类（favorite/hot/new） */
    @Column("category")
    private String category;

    /** 排序 */
    private Integer sortOrder;

    /** 浏览次数 */
    private Integer viewCount;

    /** 收藏次数 */
    private Integer favoriteCount;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 是否删除 */
    @Column(isLogicDelete = true)
    private Integer deleted;
}
