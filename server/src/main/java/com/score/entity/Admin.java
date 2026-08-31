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
 * 管理员实体类
 *
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("admin")
public class Admin {

    /** 管理员ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 用户名 */
    @Column("username")
    private String username;

    /** 密码 */
    @Column("password")
    private String password;

    /** 昵称 */
    private String nickname;

    /** 角色：super_admin / admin */
    private String role;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}