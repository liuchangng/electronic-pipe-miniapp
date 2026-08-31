package com.score.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.score.entity.HotSearch;
import com.score.entity.SearchHistory;
import com.score.entity.table.HotSearchTableDef;
import com.score.entity.table.SearchHistoryTableDef;
import com.score.mapper.HotSearchMapper;
import com.score.mapper.SearchHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索服务
 *
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class SearchService {

    private static final SearchHistoryTableDef SH = SearchHistoryTableDef.SEARCH_HISTORY;
    private static final HotSearchTableDef HS = HotSearchTableDef.HOT_SEARCH;

    @Autowired
    private SearchHistoryMapper searchHistoryMapper;

    @Autowired
    private HotSearchMapper hotSearchMapper;

    /**
     * 获取热门搜索关键词
     */
    public List<String> getHotSearches() {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(HS.IS_HOT.eq(1))
                .and(HS.DELETED.eq(0))
                .orderBy(HS.SORT_ORDER.asc(), HS.SEARCH_COUNT.desc())
                .limit(10);
        List<HotSearch> list = hotSearchMapper.selectListByQuery(wrapper);
        return list.stream().map(HotSearch::getKeyword).collect(Collectors.toList());
    }

    /**
     * 获取用户搜索历史
     */
    public List<String> getSearchHistory(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SH.USER_ID.eq(userId))
                .and(SH.DELETED.eq(0))
                .orderBy(SH.LAST_SEARCH_TIME.desc())
                .limit(20);
        List<SearchHistory> list = searchHistoryMapper.selectListByQuery(wrapper);
        return list.stream().map(SearchHistory::getKeyword).collect(Collectors.toList());
    }

    /**
     * 保存搜索历史（同一关键词累加次数）
     */
    public void saveSearchHistory(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return;
        keyword = keyword.trim();

        // 查找是否已有该关键词
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SH.USER_ID.eq(userId))
                .and(SH.KEYWORD.eq(keyword))
                .and(SH.DELETED.eq(0))
                .limit(1);
        SearchHistory existing = searchHistoryMapper.selectOneByQuery(wrapper);

        if (existing != null) {
            // 更新次数和时间
            existing.setSearchCount(existing.getSearchCount() + 1);
            existing.setLastSearchTime(LocalDateTime.now());
            searchHistoryMapper.update(existing);
        } else {
            // 新增记录
            SearchHistory history = SearchHistory.builder()
                    .userId(userId)
                    .keyword(keyword)
                    .searchCount(1)
                    .lastSearchTime(LocalDateTime.now())
                    .deleted(0)
                    .build();
            searchHistoryMapper.insert(history);
        }

        // 同时更新热门搜索计数
        updateHotSearchCount(keyword);
    }

    /**
     * 清空用户搜索历史
     */
    public void clearSearchHistory(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SH.USER_ID.eq(userId))
                .and(SH.DELETED.eq(0));
        List<SearchHistory> list = searchHistoryMapper.selectListByQuery(wrapper);
        for (SearchHistory h : list) {
            h.setDeleted(1);
            searchHistoryMapper.update(h);
        }
    }

    /**
     * 更新热门搜索计数
     */
    private void updateHotSearchCount(String keyword) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(HS.KEYWORD.eq(keyword))
                .and(HS.DELETED.eq(0))
                .limit(1);
        HotSearch existing = hotSearchMapper.selectOneByQuery(wrapper);

        if (existing != null) {
            existing.setSearchCount(existing.getSearchCount() + 1);
            hotSearchMapper.update(existing);
        } else {
            HotSearch hotSearch = HotSearch.builder()
                    .keyword(keyword)
                    .searchCount(1)
                    .isHot(0)
                    .sortOrder(0)
                    .deleted(0)
                    .build();
            hotSearchMapper.insert(hotSearch);
        }
    }
}