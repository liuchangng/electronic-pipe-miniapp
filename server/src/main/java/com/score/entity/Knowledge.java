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
 * 知识实体类
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("knowledge")
public class Knowledge {

    /** 知识ID */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /** 标题 */
    @Column("title")
    private String title;

    /** 副标题 */
    private String subtitle;

    /** 分类（introduction/history/brand/fingering） */
    @Column("category")
    private String category;

    /** 内容（HTML） */
    private String content;

    /** 排序 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 是否删除 */
    @Column(isLogicDelete = true)
    private Integer deleted;
}
