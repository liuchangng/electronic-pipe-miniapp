package com.score.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.score.dto.PageResult;
import com.score.entity.Song;
import com.score.entity.table.SongTableDef;
import com.score.mapper.SongMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 曲谱服务
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class SongService {

    private static final SongTableDef SONG = SongTableDef.SONG;

    @Autowired
    private SongMapper songMapper;

    /**
     * 根据ID获取曲谱
     * 
     * @param songId 曲谱ID
     * @return 曲谱信息
     */
    public Song getById(Long songId) {
        return songMapper.selectOneById(songId);
    }

    /**
     * 获取所有曲谱（用于下拉选择）
     */
    public List<Song> listAll() {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SONG.DELETED.eq(0))
                .orderBy(SONG.SORT_ORDER.asc());
        return songMapper.selectListByQuery(wrapper);
    }

    /**
     * 根据关键词搜索曲谱
     * 
     * @param keyword 搜索关键词
     * @return 曲谱列表
     */
    public List<Song> searchByKeyword(String keyword) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SONG.TITLE.like("%" + keyword + "%"))
                .or(SONG.AUTHOR.like("%" + keyword + "%"))
                .orderBy(SONG.SORT_ORDER.asc());
        
        return songMapper.selectListByQuery(wrapper);
    }

    /**
     * 获取热门曲谱（按收藏数排序）
     */
    public List<Song> listHot() {
        QueryWrapper wrapper = QueryWrapper.create()
                .orderBy(SONG.FAVORITE_COUNT.desc());
        
        return songMapper.selectListByQuery(wrapper);
    }

    /**
     * 获取最新曲谱（按创建时间排序）
     */
    public List<Song> listNew() {
        QueryWrapper wrapper = QueryWrapper.create()
                .orderBy(SONG.CREATED_AT.desc());
        
        return songMapper.selectListByQuery(wrapper);
    }

    /**
     * 小程序端曲谱分页列表
     */
    public PageResult<Song> pageList(String keyword, String tab, int page, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.where(SONG.TITLE.like("%" + keyword + "%"))
                   .or(SONG.AUTHOR.like("%" + keyword + "%"));
        }
        switch (tab != null ? tab : "hot") {
            case "new":
                wrapper.orderBy(SONG.CREATED_AT.desc());
                break;
            default:
                wrapper.orderBy(SONG.FAVORITE_COUNT.desc());
                break;
        }
        Page<Song> result = songMapper.paginate(Page.of(page, pageSize), wrapper);
        return PageResult.of(result.getRecords(), result.getTotalRow(), page, pageSize);
    }

    /**
     * 更新曲谱
     * 
     * @param song 曲谱信息
     */
    public void updateById(Song song) {
        songMapper.update(song);
    }

    /**
     * 根据ID列表获取曲谱列表
     * 
     * @param songIds 曲谱ID列表
     * @return 曲谱列表
     */
    public List<Song> listByIds(List<Long> songIds) {
        if (songIds == null || songIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SONG.ID.in(songIds));
        return songMapper.selectListByQuery(wrapper);
    }

    /**
     * 管理端曲谱列表（含全部状态）
     */
    public List<Song> adminList(String keyword, String category, Integer status) {
        QueryWrapper wrapper = buildAdminQueryWrapper(keyword, category, status);
        return songMapper.selectListByQuery(wrapper);
    }

    /**
     * 管理端曲谱分页列表
     */
    public PageResult<Song> adminPageList(String keyword, String category, Integer status, int page, int pageSize) {
        QueryWrapper wrapper = buildAdminQueryWrapper(keyword, category, status);
        Page<Song> result = songMapper.paginate(Page.of(page, pageSize), wrapper);
        return PageResult.of(result.getRecords(), result.getTotalRow(), page, pageSize);
    }

    private QueryWrapper buildAdminQueryWrapper(String keyword, String category, Integer status) {
        QueryWrapper wrapper = QueryWrapper.create();
        boolean hasCondition = false;
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.where(SONG.TITLE.like("%" + keyword + "%")
                    .or(SONG.AUTHOR.like("%" + keyword + "%")));
            hasCondition = true;
        }
        if (category != null && !category.isEmpty()) {
            if (hasCondition) {
                wrapper.and(SONG.CATEGORY.eq(category));
            } else {
                wrapper.where(SONG.CATEGORY.eq(category));
                hasCondition = true;
            }
        }
        if (status != null) {
            if (hasCondition) {
                wrapper.and(SONG.STATUS.eq(status));
            } else {
                wrapper.where(SONG.STATUS.eq(status));
            }
        }
        wrapper.orderBy(SONG.CREATED_AT.desc());
        return wrapper;
    }

    /**
     * 新增曲谱
     */
    public void addSong(Song song) {
        song.setCreatedAt(LocalDateTime.now());
        song.setUpdatedAt(LocalDateTime.now());
        if (song.getStatus() == null) song.setStatus(1);
        if (song.getDeleted() == null) song.setDeleted(0);
        if (song.getViewCount() == null) song.setViewCount(0);
        if (song.getFavoriteCount() == null) song.setFavoriteCount(0);
        songMapper.insert(song);
    }

    /**
     * 更新曲谱
     */
    public void updateSong(Song song) {
        song.setUpdatedAt(LocalDateTime.now());
        songMapper.update(song);
    }

    /**
     * 删除曲谱（逻辑删除）
     */
    public void deleteSong(Long id) {
        Song song = songMapper.selectOneById(id);
        if (song != null) {
            song.setDeleted(1);
            song.setUpdatedAt(LocalDateTime.now());
            songMapper.update(song);
        }
    }

    /**
     * 统计曲谱数量
     */
    public long count() {
        return songMapper.selectCountByQuery(QueryWrapper.create());
    }

    /**
     * 增加曲谱浏览次数
     *
     * @param songId 曲谱ID
     */
    public void incrementViewCount(Long songId) {
        Song song = songMapper.selectOneById(songId);
        if (song != null) {
            song.setViewCount(song.getViewCount() != null ? song.getViewCount() + 1 : 1);
            songMapper.update(song);
        }
    }

    /**
     * 统计总浏览量
     */
    public long totalViewCount() {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(SONG.VIEW_COUNT)
                .where(SONG.DELETED.eq(0));
        List<Song> songs = songMapper.selectListByQuery(wrapper);
        return songs.stream()
                .filter(s -> s != null && s.getViewCount() != null)
                .mapToLong(Song::getViewCount)
                .sum();
    }
}