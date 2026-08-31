package com.score.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.score.common.R;
import com.score.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 搜索控制器
 *
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 获取热门搜索关键词
     */
    @GetMapping("/hot")
    public R<List<String>> getHotSearches() {
        return R.ok(searchService.getHotSearches());
    }

    /**
     * 获取当前用户搜索历史
     */
    @GetMapping("/history")
    public R<List<String>> getSearchHistory() {
        long userId = StpUtil.getLoginIdAsLong();
        return R.ok(searchService.getSearchHistory(userId));
    }

    /**
     * 保存搜索历史
     */
    @PostMapping("/history")
    public R saveSearchHistory(@RequestBody Map<String, String> request) {
        String keyword = request.get("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            return R.fail("关键词不能为空");
        }
        long userId = StpUtil.getLoginIdAsLong();
        searchService.saveSearchHistory(userId, keyword.trim());
        return R.ok("保存成功");
    }

    /**
     * 清空搜索历史
     */
    @DeleteMapping("/history")
    public R clearSearchHistory() {
        long userId = StpUtil.getLoginIdAsLong();
        searchService.clearSearchHistory(userId);
        return R.ok("清空成功");
    }
}