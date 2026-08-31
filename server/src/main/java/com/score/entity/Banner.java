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
 * Banner实体类
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("banner")
public class Banner {

    /** Banner ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 标题 */
    private String title;

    /** 图片URL */
    @Column("image_url")
    private String imageUrl;

    /** 链接URL */
    private String linkUrl;

    /** 排序 */
    private Integer sortOrder;

    /** 状态（1-启用，0-禁用） */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 是否删除 */
    @Column(isLogicDelete = true)
    private Integer deleted;
}
