package com.score.mapper;

import com.mybatisflex.core.BaseMapper;
import com.score.entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 搜索历史 Mapper
 *
 * @author Score Team
 * @since 1.0.0
 */
@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {
}