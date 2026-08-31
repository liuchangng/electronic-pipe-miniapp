package com.score.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.score.entity.User;
import com.score.entity.Video;
import com.score.entity.VideoFavorite;
import com.score.entity.table.VideoFavoriteTableDef;
import com.score.mapper.VideoFavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 视频收藏服务
 *
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class VideoFavoriteService {

    private static final VideoFavoriteTableDef VF = VideoFavoriteTableDef.VIDEO_FAVORITE;

    @Autowired
    private VideoFavoriteMapper videoFavoriteMapper;

    @Autowired
    private VideoService videoService;

    @Autowired
    private PointService pointService;

    @Autowired
    private UserService userService;

    /**
     * 收藏视频（记录直增不减，status标记状态）
     */
    public void addFavorite(Long userId, Long videoId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VF.USER_ID.eq(userId))
                .and(VF.VIDEO_ID.eq(videoId));

        VideoFavorite existing = videoFavoriteMapper.selectOneByQuery(wrapper);
        if (existing == null) {
            // 首次收藏，创建新记录
            VideoFavorite favorite = new VideoFavorite();
            favorite.setUserId(userId);
            favorite.setVideoId(videoId);
            favorite.setStatus("active");
            favorite.setCreatedAt(LocalDateTime.now());
            favorite.setUpdatedAt(LocalDateTime.now());
            videoFavoriteMapper.insert(favorite);

            // 更新视频收藏数
            Video video = videoService.getById(videoId);
            if (video != null) {
                video.setFavoriteCount(video.getFavoriteCount() + 1);
                videoService.updateById(video);
            }

            // 积分：收藏+2
            pointService.addFavoritePoint(userId);
        } else if ("cancelled".equals(existing.getStatus())) {
            // 之前取消过，重新激活
            existing.setStatus("active");
            existing.setUpdatedAt(LocalDateTime.now());
            videoFavoriteMapper.update(existing);

            // 更新视频收藏数
            Video video = videoService.getById(videoId);
            if (video != null) {
                video.setFavoriteCount(video.getFavoriteCount() + 1);
                videoService.updateById(video);
            }

            // 积分：收藏+2
            pointService.addFavoritePoint(userId);
        }
        // 已是active状态，无需操作
    }

    /**
     * 取消收藏（标记cancelled，不删除记录）
     */
    public void removeFavorite(Long userId, Long videoId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VF.USER_ID.eq(userId))
                .and(VF.VIDEO_ID.eq(videoId));

        VideoFavorite existing = videoFavoriteMapper.selectOneByQuery(wrapper);
        if (existing != null && "active".equals(existing.getStatus())) {
            existing.setStatus("cancelled");
            existing.setUpdatedAt(LocalDateTime.now());
            videoFavoriteMapper.update(existing);

            // 更新视频收藏数
            Video video = videoService.getById(videoId);
            if (video != null && video.getFavoriteCount() > 0) {
                video.setFavoriteCount(video.getFavoriteCount() - 1);
                videoService.updateById(video);
            }
        }
    }

    /**
     * 切换收藏状态
     * @return true=已收藏, false=已取消
     */
    public boolean toggleFavorite(Long userId, Long videoId) {
        if (isFavorited(userId, videoId)) {
            removeFavorite(userId, videoId);
            return false;
        } else {
            addFavorite(userId, videoId);
            return true;
        }
    }

    /**
     * 检查是否已收藏（只看active状态）
     */
    public boolean isFavorited(Long userId, Long videoId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VF.USER_ID.eq(userId))
                .and(VF.VIDEO_ID.eq(videoId))
                .and(VF.STATUS.eq("active"));
        return videoFavoriteMapper.selectCountByQuery(wrapper) > 0;
    }

    /**
     * 获取用户收藏的视频ID列表（只看active状态）
     */
    public List<Long> getFavoritedVideoIds(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VF.USER_ID.eq(userId))
                .and(VF.STATUS.eq("active"));
        return videoFavoriteMapper.selectListByQuery(wrapper).stream()
                .map(VideoFavorite::getVideoId)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户收藏的视频详情列表（只看active状态）
     */
    public List<Video> getFavoritedVideos(Long userId) {
        List<Long> videoIds = getFavoritedVideoIds(userId);
        if (videoIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return videoService.listByIds(videoIds);
    }

    /**
     * 获取视频的收藏用户列表（active状态）
     */
    public List<User> getFavoriteUsers(Long videoId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VF.VIDEO_ID.eq(videoId))
                .and(VF.STATUS.eq("active"))
                .orderBy(VF.CREATED_AT.desc());

        List<VideoFavorite> favorites = videoFavoriteMapper.selectListByQuery(wrapper);
        List<Long> userIds = favorites.stream().map(VideoFavorite::getUserId).collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return userService.listByIds(userIds);
    }
}