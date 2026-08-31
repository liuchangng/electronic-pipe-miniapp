package com.score.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.score.entity.User;
import com.score.entity.Video;
import com.score.entity.VideoLike;
import com.score.entity.table.VideoLikeTableDef;
import com.score.mapper.VideoLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 视频点赞服务
 *
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class VideoLikeService {

    private static final VideoLikeTableDef VL = VideoLikeTableDef.VIDEO_LIKE;

    @Autowired
    private VideoLikeMapper videoLikeMapper;

    @Autowired
    private VideoService videoService;

    @Autowired
    private PointService pointService;

    @Autowired
    private UserService userService;

    /**
     * 点赞视频（记录直增不减，status标记状态）
     */
    public void addLike(Long userId, Long videoId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VL.USER_ID.eq(userId))
                .and(VL.VIDEO_ID.eq(videoId));

        VideoLike existing = videoLikeMapper.selectOneByQuery(wrapper);
        if (existing == null) {
            // 首次点赞，创建新记录
            VideoLike like = new VideoLike();
            like.setUserId(userId);
            like.setVideoId(videoId);
            like.setStatus("active");
            like.setCreatedAt(LocalDateTime.now());
            like.setUpdatedAt(LocalDateTime.now());
            videoLikeMapper.insert(like);

            // 更新视频点赞数
            Video video = videoService.getById(videoId);
            if (video != null) {
                video.setLikeCount(video.getLikeCount() + 1);
                videoService.updateById(video);
            }

            // 积分：点赞+1
            pointService.addLikePoint(userId);
        } else if ("cancelled".equals(existing.getStatus())) {
            // 之前取消过，重新激活
            existing.setStatus("active");
            existing.setUpdatedAt(LocalDateTime.now());
            videoLikeMapper.update(existing);

            // 更新视频点赞数
            Video video = videoService.getById(videoId);
            if (video != null) {
                video.setLikeCount(video.getLikeCount() + 1);
                videoService.updateById(video);
            }

            // 积分：点赞+1
            pointService.addLikePoint(userId);
        }
        // 已是active状态，无需操作
    }

    /**
     * 取消点赞（标记cancelled，不删除记录）
     */
    public void removeLike(Long userId, Long videoId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VL.USER_ID.eq(userId))
                .and(VL.VIDEO_ID.eq(videoId));

        VideoLike existing = videoLikeMapper.selectOneByQuery(wrapper);
        if (existing != null && "active".equals(existing.getStatus())) {
            existing.setStatus("cancelled");
            existing.setUpdatedAt(LocalDateTime.now());
            videoLikeMapper.update(existing);

            // 更新视频点赞数
            Video video = videoService.getById(videoId);
            if (video != null && video.getLikeCount() > 0) {
                video.setLikeCount(video.getLikeCount() - 1);
                videoService.updateById(video);
            }
        }
    }

    /**
     * 切换点赞状态
     * @return true=已点赞, false=已取消
     */
    public boolean toggleLike(Long userId, Long videoId) {
        if (isLiked(userId, videoId)) {
            removeLike(userId, videoId);
            return false;
        } else {
            addLike(userId, videoId);
            return true;
        }
    }

    /**
     * 检查是否已点赞（只看active状态）
     */
    public boolean isLiked(Long userId, Long videoId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VL.USER_ID.eq(userId))
                .and(VL.VIDEO_ID.eq(videoId))
                .and(VL.STATUS.eq("active"));
        return videoLikeMapper.selectCountByQuery(wrapper) > 0;
    }

    /**
     * 获取用户点赞的视频ID列表（只看active状态）
     */
    public List<Long> getLikedVideoIds(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VL.USER_ID.eq(userId))
                .and(VL.STATUS.eq("active"));
        return videoLikeMapper.selectListByQuery(wrapper).stream()
                .map(VideoLike::getVideoId)
                .collect(Collectors.toList());
    }

    /**
     * 获取视频的点赞用户列表（active状态）
     */
    public List<User> getLikeUsers(Long videoId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VL.VIDEO_ID.eq(videoId))
                .and(VL.STATUS.eq("active"))
                .orderBy(VL.CREATED_AT.desc());

        List<VideoLike> likes = videoLikeMapper.selectListByQuery(wrapper);
        List<Long> userIds = likes.stream().map(VideoLike::getUserId).collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return userService.listByIds(userIds);
    }
}