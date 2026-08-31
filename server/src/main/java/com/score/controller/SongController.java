package com.score.controller;

import com.score.common.R;
import com.score.entity.Song;
import com.score.service.SongService;
import com.score.service.StorageService;
import com.score.dto.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 曲谱控制器
 * 
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/song")
public class SongController {

    @Autowired
    private SongService songService;

    @Autowired
    private StorageService storageService;

    /**
     * 获取曲谱列表
     * 
     * @param keyword 搜索关键词
     * @param tab 切换类型（hot/new/favorite）
     * @return 曲谱列表
     */
    @GetMapping("/list")
    public R<List<Song>> getSongList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "hot") String tab) {

        List<Song> songs;
        if (keyword != null && !keyword.isEmpty()) {
            songs = songService.searchByKeyword(keyword);
        } else {
            switch (tab) {
                case "new":
                    songs = songService.listNew();
                    break;
                default:
                    songs = songService.listHot();
                    break;
            }
        }

        storageService.fillSongUrls(songs);
        return R.ok(songs);
    }

    /**
     * 曲谱分页列表（小程序端）
     */
    @GetMapping("/page")
    public R<PageResult<Song>> getSongPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "hot") String tab,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResult<Song> result = songService.pageList(keyword, tab, page, pageSize);
        storageService.fillSongUrls(result.getList());
        return R.ok(result);
    }

    /**
     * 获取曲谱详情
     * 
     * @param id 曲谱ID
     * @return 曲谱详情
     */
    @GetMapping("/detail/{id}")
    public R<Song> getSongDetail(@PathVariable Long id) {
        Song song = songService.getById(id);
        if (song == null) {
            return R.fail("曲谱不存在");
        }
        // 增加浏览次数
        songService.incrementViewCount(id);
        // 重新获取更新后的数据
        song = songService.getById(id);
        storageService.fillSongUrls(song);
        return R.ok(song);
    }
}
