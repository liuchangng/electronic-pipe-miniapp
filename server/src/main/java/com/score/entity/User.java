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
 * 用户实体类
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user")
public class User {

    /** 用户ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 微信openid */
    @Column("openid")
    private String openid;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 积分 */
    private Integer point;

    /** 等级 */
    private Integer level;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 是否删除（0-否，1-是） */
    @Column(isLogicDelete = true)
    private Integer deleted;
}
