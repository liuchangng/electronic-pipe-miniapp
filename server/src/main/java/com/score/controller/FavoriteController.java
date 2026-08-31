package com.score.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.score.common.R;
import com.score.entity.Song;
import com.score.entity.User;
import com.score.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏控制器
 * 
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 获取用户收藏的曲谱ID列表
     * 
     * @return 收藏的曲谱ID列表
     */
    @GetMapping("/list")
    public R<List<Long>> getFavoriteList() {
        long userId = StpUtil.getLoginIdAsLong();
        List<Long> songIds = favoriteService.getFavoriteSongIds(userId);
        return R.ok(songIds);
    }

    /**
     * 获取用户收藏的曲谱详情列表
     * 
     * @return 收藏的曲谱列表
     */
    @GetMapping("/detail")
    public R<List<Song>> getFavoriteDetail() {
        long userId = StpUtil.getLoginIdAsLong();
        List<Song> songs = favoriteService.getFavoriteSongs(userId);
        return R.ok(songs);
    }

    /**
     * 添加收藏
     * 
     * @param request 请求体（songId: 曲谱ID）
     * @return 操作结果
     */
    @PostMapping("/add")
    public R addFavorite(@RequestBody java.util.Map<String, Long> request) {
        long userId = StpUtil.getLoginIdAsLong();
        Long songId = request.get("songId");
        
        if (songId == null) {
            return R.fail("曲谱ID不能为空");
        }

        // 检查是否已收藏
        if (favoriteService.isFavorite(userId, songId)) {
            return R.fail("已收藏");
        }

        favoriteService.addFavorite(userId, songId);
        return R.ok("收藏成功");
    }

    /**
     * 取消收藏
     * 
     * @param request 请求体（songId: 曲谱ID）
     * @return 操作结果
     */
    @PostMapping("/remove")
    public R removeFavorite(@RequestBody java.util.Map<String, Long> request) {
        long userId = StpUtil.getLoginIdAsLong();
        Long songId = request.get("songId");
        
        if (songId == null) {
            return R.fail("曲谱ID不能为空");
        }

        favoriteService.removeFavorite(userId, songId);
        return R.ok("取消收藏成功");
    }

    /**
     * 获取曲谱的收藏用户列表（需登录）
     */
    @GetMapping("/users")
    public R<List<Map<String, Object>>> getFavoriteUsers(@RequestParam Long songId) {
        List<User> users = favoriteService.getFavoriteUsers(songId);
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("nickname", u.getNickname());
            m.put("avatar", u.getAvatar());
            return m;
        }).collect(Collectors.toList());
        return R.ok(result);
    }
}
