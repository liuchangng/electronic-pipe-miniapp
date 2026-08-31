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
 * 搜索历史实体类
 *
 * @author Score Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("search_history")
public class SearchHistory {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long userId;

    private String keyword;

    @Column("search_count")
    private Integer searchCount;

    @Column("last_search_time")
    private LocalDateTime lastSearchTime;

    @Column("created_at")
    private LocalDateTime createdAt;

    private Integer deleted;
}