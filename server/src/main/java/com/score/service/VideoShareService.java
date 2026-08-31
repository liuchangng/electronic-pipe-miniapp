package com.score.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.score.entity.VideoShare;
import com.score.entity.table.VideoShareTableDef;
import com.score.mapper.VideoShareMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 视频分享记录服务
 *
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class VideoShareService {

    private static final VideoShareTableDef VS = VideoShareTableDef.VIDEO_SHARE;

    @Autowired
    private VideoShareMapper videoShareMapper;

    @Autowired
    private VideoService videoService;

    /**
     * 记录分享（每次分享都新增一条记录）
     */
    public void recordShare(Long userId, Long videoId) {
        VideoShare share = new VideoShare();
        share.setUserId(userId);
        share.setVideoId(videoId);
        share.setCreatedAt(LocalDateTime.now());
        videoShareMapper.insert(share);

        // 更新视频分享数
        videoService.incrementShareCount(videoId);
    }

    /**
     * 获取视频的分享记录数
     */
    public long getShareCount(Long videoId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VS.VIDEO_ID.eq(videoId));
        return videoShareMapper.selectCountByQuery(wrapper);
    }

    /**
     * 获取视频的分享记录列表
     */
    public List<VideoShare> getShareList(Long videoId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VS.VIDEO_ID.eq(videoId))
                .orderBy(VS.CREATED_AT.desc());
        return videoShareMapper.selectListByQuery(wrapper);
    }

    /**
     * 获取用户的分享视频ID列表
     */
    public List<Long> getUserSharedVideoIds(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VS.USER_ID.eq(userId))
                .orderBy(VS.CREATED_AT.desc());
        return videoShareMapper.selectListByQuery(wrapper).stream()
                .map(VideoShare::getVideoId)
                .distinct()
                .collect(Collectors.toList());
    }
}