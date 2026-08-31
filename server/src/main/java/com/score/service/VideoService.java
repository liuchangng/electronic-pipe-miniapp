package com.score.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.score.dto.PageResult;
import com.score.entity.Video;
import com.score.entity.Song;
import com.score.entity.table.VideoTableDef;
import com.score.mapper.VideoMapper;
import com.score.mapper.SongMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * 视频服务
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class VideoService {

    private static final VideoTableDef VIDEO = VideoTableDef.VIDEO;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private SongMapper songMapper;

    /**
     * 根据曲谱ID获取视频列表
     * 
     * @param songId 曲谱ID
     * @return 视频列表
     */
    public List<Video> listBySongId(Long songId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VIDEO.SONG_ID.eq(songId))
                .and(VIDEO.DELETED.eq(0))
                .orderBy(VIDEO.SORT_ORDER.asc());
        
        return videoMapper.selectListByQuery(wrapper);
    }

    /**
     * 获取全部视频列表
     * 
     * @return 视频列表
     */
    public List<Video> listAll() {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VIDEO.DELETED.eq(0))
                .orderBy(VIDEO.SORT_ORDER.asc());
        
        return videoMapper.selectListByQuery(wrapper);
    }

    /**
     * 获取视频详情
     * 
     * @param videoId 视频ID
     * @return 视频信息
     */
    public Video getVideoDetail(Long videoId) {
        return videoMapper.selectOneById(videoId);
    }

    /**
     * 根据ID获取视频
     *
     * @param videoId 视频ID
     * @return 视频信息
     */
    public Video getById(Long videoId) {
        return videoMapper.selectOneById(videoId);
    }

    /**
     * 更新视频（按ID）
     *
     * @param video 视频实体
     */
    public void updateById(Video video) {
        video.setUpdatedAt(LocalDateTime.now());
        videoMapper.update(video);
    }

    /**
     * 根据ID列表批量查询视频
     *
     * @param ids 视频ID列表
     * @return 视频列表
     */
    public List<Video> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VIDEO.ID.in(ids))
                .and(VIDEO.DELETED.eq(0));
        return videoMapper.selectListByQuery(wrapper);
    }

    /**
     * 管理端视频列表
     */
    public List<Video> adminList(String keyword, Long songId) {
        QueryWrapper wrapper = buildAdminQueryWrapper(keyword, songId);
        return videoMapper.selectListByQuery(wrapper);
    }

    /**
     * 管理端视频分页列表
     */
    public PageResult<Video> adminPageList(String keyword, Long songId, int page, int pageSize) {
        QueryWrapper wrapper = buildAdminQueryWrapper(keyword, songId);
        Page<Video> result = videoMapper.paginate(Page.of(page, pageSize), wrapper);
        List<Video> videos = result.getRecords();
        // 填充关联曲谱名称
        for (Video video : videos) {
            if (video.getSongId() != null) {
                Song song = songMapper.selectOneById(video.getSongId());
                if (song != null) {
                    video.setSongTitle(song.getTitle());
                }
            }
        }
        return PageResult.of(videos, result.getTotalRow(), page, pageSize);
    }

    private QueryWrapper buildAdminQueryWrapper(String keyword, Long songId) {
        QueryWrapper wrapper = QueryWrapper.create();
        boolean hasCondition = false;
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.where(VIDEO.TITLE.like("%" + keyword + "%"));
            hasCondition = true;
        }
        if (songId != null) {
            if (hasCondition) {
                wrapper.and(VIDEO.SONG_ID.eq(songId));
            } else {
                wrapper.where(VIDEO.SONG_ID.eq(songId));
            }
        }
        wrapper.orderBy(VIDEO.CREATED_AT.desc());
        return wrapper;
    }

    /**
     * 新增视频
     */
    public void addVideo(Video video) {
        video.setCreatedAt(LocalDateTime.now());
        video.setUpdatedAt(LocalDateTime.now());
        if (video.getDeleted() == null) video.setDeleted(0);
        if (video.getDuration() == null) video.setDuration(0);
        if (video.getSortOrder() == null) video.setSortOrder(0);
        videoMapper.insert(video);
    }

    /**
     * 更新视频
     */
    public void updateVideo(Video video) {
        video.setUpdatedAt(LocalDateTime.now());
        videoMapper.update(video);
    }

    /**
     * 删除视频（逻辑删除）
     */
    public void deleteVideo(Long id) {
        Video video = videoMapper.selectOneById(id);
        if (video != null) {
            video.setDeleted(1);
            video.setUpdatedAt(LocalDateTime.now());
            videoMapper.update(video);
        }
    }

    /**
     * 统计视频数量
     */
    public long count() {
        return videoMapper.selectCountByQuery(QueryWrapper.create());
    }

    /**
     * 增加视频播放次数
     *
     * @param videoId 视频ID
     */
    public void incrementPlayCount(Long videoId) {
        Video video = videoMapper.selectOneById(videoId);
        if (video != null) {
            video.setPlayCount(video.getPlayCount() != null ? video.getPlayCount() + 1 : 1);
            videoMapper.update(video);
        }
    }

    /**
     * 增加视频评论数
     *
     * @param videoId 视频ID
     */
    public void incrementCommentCount(Long videoId) {
        Video video = videoMapper.selectOneById(videoId);
        if (video != null) {
            video.setCommentCount(video.getCommentCount() != null ? video.getCommentCount() + 1 : 1);
            videoMapper.update(video);
        }
    }

    /**
     * 减少视频评论数
     *
     * @param videoId 视频ID
     */
    public void decrementCommentCount(Long videoId) {
        Video video = videoMapper.selectOneById(videoId);
        if (video != null && video.getCommentCount() != null && video.getCommentCount() > 0) {
            video.setCommentCount(video.getCommentCount() - 1);
            videoMapper.update(video);
        }
    }

    /**
     * 增加视频分享数
     *
     * @param videoId 视频ID
     */
    public void incrementShareCount(Long videoId) {
        Video video = videoMapper.selectOneById(videoId);
        if (video != null) {
            video.setShareCount(video.getShareCount() != null ? video.getShareCount() + 1 : 1);
            videoMapper.update(video);
        }
    }

    /**
     * 统计总播放量
     */
    public long totalPlayCount() {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(VIDEO.PLAY_COUNT)
                .where(VIDEO.DELETED.eq(0));
        List<Video> videos = videoMapper.selectListByQuery(wrapper);
        return videos.stream()
                .filter(v -> v != null && v.getPlayCount() != null)
                .mapToLong(Video::getPlayCount)
                .sum();
    }

    /**
     * 查询待转码的视频列表（pending状态，按创建时间升序）
     */
    public List<Video> listPendingTranscode() {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VIDEO.TRANSCODE_STATUS.eq("pending"))
                .and(VIDEO.DELETED.eq(0))
                .orderBy(VIDEO.CREATED_AT.asc());
        return videoMapper.selectListByQuery(wrapper);
    }

    /**
     * 客户端视频分页列表（支持按曲谱筛选）
     *
     * @param songId   曲谱ID（可选，null则返回全部）
     * @param page     页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    public PageResult<Video> pageList(Long songId, int page, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VIDEO.DELETED.eq(0));
        if (songId != null) {
            wrapper.and(VIDEO.SONG_ID.eq(songId));
        }
        wrapper.orderBy(VIDEO.SORT_ORDER.asc());
        Page<Video> result = videoMapper.paginate(Page.of(page, pageSize), wrapper);
        return PageResult.of(result.getRecords(), result.getTotalRow(), page, pageSize);
    }

    /**
     * 获取有视频关联的曲谱列表（去重，按曲谱排序）
     * 
     * @return 有视频的曲谱列表
     */
    public List<Song> listSongsWithVideo() {
        // 查询所有未删除的视频，提取关联的曲谱ID
        QueryWrapper wrapper = QueryWrapper.create()
                .where(VIDEO.DELETED.eq(0))
                .and(VIDEO.SONG_ID.isNotNull())
                .orderBy(VIDEO.SONG_ID.asc());
        List<Video> videos = videoMapper.selectListByQuery(wrapper);

        // 去重曲谱ID
        LinkedHashSet<Long> songIds = new LinkedHashSet<>();
        for (Video video : videos) {
            if (video.getSongId() != null) {
                songIds.add(video.getSongId());
            }
        }

        // 批量查询曲谱
        List<Song> songs = new ArrayList<>();
        for (Long songId : songIds) {
            Song song = songMapper.selectOneById(songId);
            if (song != null && song.getDeleted() == 0) {
                songs.add(song);
            }
        }
        return songs;
    }
}