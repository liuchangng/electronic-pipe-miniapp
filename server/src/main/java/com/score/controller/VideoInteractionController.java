package com.score.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.score.common.R;
import com.score.entity.User;
import com.score.entity.Video;
import com.score.service.VideoFavoriteService;
import com.score.service.VideoLikeService;
import com.score.service.VideoService;
import com.score.service.VideoShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 视频互动控制器（点赞、收藏、分享）
 *
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/video")
public class VideoInteractionController {

    @Autowired
    private VideoLikeService videoLikeService;

    @Autowired
    private VideoFavoriteService videoFavoriteService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoShareService videoShareService;

    // ==================== 点赞 ====================

    /**
     * 切换点赞状态（需登录）
     * @param request { videoId: 视频ID }
     * @return { liked: true/false }
     */
    @PostMapping("/like/toggle")
    public R<Map<String, Object>> toggleLike(@RequestBody Map<String, Long> request) {
        long userId = StpUtil.getLoginIdAsLong();
        Long videoId = request.get("videoId");
        if (videoId == null) {
            return R.fail("视频ID不能为空");
        }
        boolean liked = videoLikeService.toggleLike(userId, videoId);
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        return R.ok(result);
    }

    /**
     * 检查是否已点赞（需登录）
     */
    @GetMapping("/like/check")
    public R<Map<String, Object>> checkLike(@RequestParam Long videoId) {
        long userId = StpUtil.getLoginIdAsLong();
        boolean liked = videoLikeService.isLiked(userId, videoId);
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        return R.ok(result);
    }

    /**
     * 获取用户点赞的视频ID列表（需登录）
     */
    @GetMapping("/like/batch-check")
    public R<List<Long>> getLikedVideoIds() {
        long userId = StpUtil.getLoginIdAsLong();
        List<Long> ids = videoLikeService.getLikedVideoIds(userId);
        return R.ok(ids);
    }

    /**
     * 获取视频的点赞用户列表（需登录）
     */
    @GetMapping("/like/users")
    public R<List<Map<String, Object>>> getLikeUsers(@RequestParam Long videoId) {
        List<User> users = videoLikeService.getLikeUsers(videoId);
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("nickname", u.getNickname());
            m.put("avatar", u.getAvatar());
            return m;
        }).collect(Collectors.toList());
        return R.ok(result);
    }

    // ==================== 收藏 ====================

    /**
     * 切换收藏状态（需登录）
     * @param request { videoId: 视频ID }
     * @return { favorited: true/false }
     */
    @PostMapping("/favorite/toggle")
    public R<Map<String, Object>> toggleFavorite(@RequestBody Map<String, Long> request) {
        long userId = StpUtil.getLoginIdAsLong();
        Long videoId = request.get("videoId");
        if (videoId == null) {
            return R.fail("视频ID不能为空");
        }
        boolean favorited = videoFavoriteService.toggleFavorite(userId, videoId);
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favorited);
        return R.ok(result);
    }

    /**
     * 检查是否已收藏（需登录）
     */
    @GetMapping("/favorite/check")
    public R<Map<String, Object>> checkFavorite(@RequestParam Long videoId) {
        long userId = StpUtil.getLoginIdAsLong();
        boolean favorited = videoFavoriteService.isFavorited(userId, videoId);
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favorited);
        return R.ok(result);
    }

    /**
     * 获取用户收藏的视频ID列表（需登录）
     */
    @GetMapping("/favorite/list")
    public R<List<Long>> getFavoriteList() {
        long userId = StpUtil.getLoginIdAsLong();
        List<Long> videoIds = videoFavoriteService.getFavoritedVideoIds(userId);
        return R.ok(videoIds);
    }

    /**
     * 获取用户收藏的视频详情列表（需登录）
     */
    @GetMapping("/favorite/detail")
    public R<List<Video>> getFavoriteDetail() {
        long userId = StpUtil.getLoginIdAsLong();
        List<Video> videos = videoFavoriteService.getFavoritedVideos(userId);
        return R.ok(videos);
    }

    /**
     * 获取视频的收藏用户列表（需登录）
     */
    @GetMapping("/favorite/users")
    public R<List<Map<String, Object>>> getFavoriteUsers(@RequestParam Long videoId) {
        List<User> users = videoFavoriteService.getFavoriteUsers(videoId);
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("nickname", u.getNickname());
            m.put("avatar", u.getAvatar());
            return m;
        }).collect(Collectors.toList());
        return R.ok(result);
    }

    // ==================== 分享 ====================

    /**
     * 记录分享（需登录，记录分享历史）
     * @param request { videoId: 视频ID }
     */
    @PostMapping("/share/record")
    public R<String> recordShare(@RequestBody Map<String, Long> request) {
        Long videoId = request.get("videoId");
        if (videoId == null) {
            return R.fail("视频ID不能为空");
        }
        // 尝试获取登录用户ID，分享无需强制登录
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 未登录，userId为null
        }
        videoShareService.recordShare(userId, videoId);
        return R.ok("ok");
    }
}