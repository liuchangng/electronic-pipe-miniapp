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
 * 积分流水实体类
 *
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("point_log")
public class PointLog {

    /** 流水ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 用户ID */
    @Column("user_id")
    private Long userId;

    /** 积分类型: login/view_song/favorite/like/comment/request_accepted/daily_login */
    @Column("type")
    private String type;

    /** 积分变动值（正数增加，负数减少） */
    @Column("amount")
    private Integer amount;

    /** 描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createdAt;
}