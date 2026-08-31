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
 * 用户反馈实体类
 *
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("feedback")
public class Feedback {

    /** 反馈ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 用户ID */
    @Column("user_id")
    private Long userId;

    /** 反馈类型：suggestion(建议), bug(问题), other(其他) */
    private String type;

    /** 反馈内容 */
    private String content;

    /** 联系方式（可选） */
    private String contact;

    /** 状态：pending(待处理), resolved(已处理) */
    private String status;

    /** 管理员回复 */
    private String reply;

    /** 创建时间 */
    @Column("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @Column("updated_at")
    private LocalDateTime updatedAt;
}