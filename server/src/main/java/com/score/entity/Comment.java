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
 * 评论实体类
 *
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("comment")
public class Comment {

    /** 评论ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 用户ID */
    @Column("user_id")
    private Long userId;

    /** 视频ID */
    @Column("video_id")
    private Long videoId;

    /** 评论内容 */
    private String content;

    /** 父评论ID（用于回复） */
    @Column("parent_id")
    private Long parentId;

    /** 状态: pending/approved/rejected */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 是否删除 */
    @Column(isLogicDelete = true)
    private Integer deleted;

    /** 用户昵称（非数据库字段） */
    @Column(ignore = true)
    private String nickname;

    /** 用户头像（非数据库字段） */
    @Column(ignore = true)
    private String avatar;

    /** 子评论列表（非数据库字段，用于树形展示） */
    @Column(ignore = true)
    private java.util.List<Comment> children;
}