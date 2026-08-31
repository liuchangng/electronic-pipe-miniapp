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
 * 热门搜索实体类
 *
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("hot_search")
public class HotSearch {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String keyword;

    @Column("search_count")
    private Integer searchCount;

    @Column("is_hot")
    private Integer isHot;

    @Column("sort_order")
    private Integer sortOrder;

    @Column("created_at")
    private LocalDateTime createdAt;

    private Integer deleted;
}