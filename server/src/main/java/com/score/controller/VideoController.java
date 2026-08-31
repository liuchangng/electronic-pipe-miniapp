package com.score.controller;

import com.score.common.R;
import com.score.entity.Video;
import com.score.entity.Song;
import com.score.dto.PageResult;
import com.score.service.VideoService;
import com.score.service.SongService;
import com.score.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

/**
 * 视频控制器
 * 
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private SongService songService;

    @Autowired
    private StorageService storageService;

    /**
     * 获取曲谱关联的视频列表
     * 
     * @param songId 曲谱ID
     * @return 视频列表
     */
    @GetMapping("/list")
    public R<List<Video>> getVideoList(@RequestParam Long songId) {
        List<Video> videos = videoService.listBySongId(songId);
        storageService.fillVideoUrls(videos);
        return R.ok(videos);
    }

    /**
     * 获取全部视频列表
     * 
     * @return 视频列表
     */
    @GetMapping("/all")
    public R<List<Video>> getAllVideos() {
        List<Video> videos = videoService.listAll();
        storageService.fillVideoUrls(videos);
        fillVideoSongTitles(videos);
        return R.ok(videos);
    }

    /**
     * 客户端视频分页列表（支持按曲谱筛选）
     *
     * @param songId   曲谱ID（可选）
     * @param page     页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<PageResult<Video>> getVideoPage(
            @RequestParam(required = false) Long songId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<Video> result = videoService.pageList(songId, page, pageSize);
        storageService.fillVideoUrls(result.getList());
        fillVideoSongTitles(result.getList());
        return R.ok(result);
    }

    /**
     * 获取有视频的曲谱列表（用于教程页分类筛选）
     * 
     * @return 有视频关联的曲谱列表
     */
    @GetMapping("/songs")
    public R<List<Song>> getVideoSongs() {
        List<Song> songs = videoService.listSongsWithVideo();
        storageService.fillSongUrls(songs);
        return R.ok(songs);
    }

    /**
     * 获取视频详情
     * 
     * @param id 视频ID
     * @return 视频详情
     */
    @GetMapping("/detail/{id}")
    public R<Video> getVideoDetail(@PathVariable Long id) {
        Video video = videoService.getVideoDetail(id);
        if (video == null) {
            return R.fail("视频不存在");
        }
        // 增加播放次数
        videoService.incrementPlayCount(id);
        // 重新获取更新后的数据
        video = videoService.getVideoDetail(id);
        storageService.fillVideoUrls(video);
        return R.ok(video);
    }

    /**
     * 为视频列表填充关联曲谱名称
     */
    private void fillVideoSongTitles(List<Video> videos) {
        for (Video video : videos) {
            if (video.getSongId() != null) {
                Song song = songService.getById(video.getSongId());
                if (song != null) {
                    video.setSongTitle(song.getTitle());
                }
            }
        }
    }
}
