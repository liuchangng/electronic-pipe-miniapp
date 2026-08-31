package com.score.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.score.entity.Favorite;
import com.score.entity.Song;
import com.score.entity.User;
import com.score.entity.table.FavoriteTableDef;
import com.score.mapper.FavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class FavoriteService {

    private static final FavoriteTableDef FAVORITE = FavoriteTableDef.FAVORITE;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private SongService songService;

    @Autowired
    private PointService pointService;

    @Autowired
    private UserService userService;

    /**
     * 收藏曲谱（记录直增不减，status标记状态）
     * 
     * @param userId 用户ID
     * @param songId 曲谱ID
     */
    public void addFavorite(Long userId, Long songId) {
        // 检查是否已有记录（无论active/cancelled）
        QueryWrapper wrapper = QueryWrapper.create()
                .where(FAVORITE.USER_ID.eq(userId))
                .and(FAVORITE.SONG_ID.eq(songId));
        
        Favorite existing = favoriteMapper.selectOneByQuery(wrapper);
        if (existing == null) {
            // 首次收藏，创建新记录
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setSongId(songId);
            favorite.setStatus("active");
            favorite.setCreatedAt(LocalDateTime.now());
            favorite.setUpdatedAt(LocalDateTime.now());
            favoriteMapper.insert(favorite);
            
            // 更新曲谱收藏数
            Song song = songService.getById(songId);
            if (song != null) {
                song.setFavoriteCount(song.getFavoriteCount() + 1);
                songService.updateById(song);
            }

            // 积分：收藏+2
            pointService.addFavoritePoint(userId);
        } else if ("cancelled".equals(existing.getStatus())) {
            // 之前取消过，重新激活
            existing.setStatus("active");
            existing.setUpdatedAt(LocalDateTime.now());
            favoriteMapper.update(existing);
            
            // 更新曲谱收藏数
            Song song = songService.getById(songId);
            if (song != null) {
                song.setFavoriteCount(song.getFavoriteCount() + 1);
                songService.updateById(song);
            }

            // 积分：收藏+2
            pointService.addFavoritePoint(userId);
        }
        // 已是active状态，无需操作
    }

    /**
     * 取消收藏（标记cancelled，不删除记录）
     * 
     * @param userId 用户ID
     * @param songId 曲谱ID
     */
    public void removeFavorite(Long userId, Long songId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(FAVORITE.USER_ID.eq(userId))
                .and(FAVORITE.SONG_ID.eq(songId));
        
        Favorite existing = favoriteMapper.selectOneByQuery(wrapper);
        if (existing != null && "active".equals(existing.getStatus())) {
            existing.setStatus("cancelled");
            existing.setUpdatedAt(LocalDateTime.now());
            favoriteMapper.update(existing);
            
            // 更新曲谱收藏数
            Song song = songService.getById(songId);
            if (song != null && song.getFavoriteCount() > 0) {
                song.setFavoriteCount(song.getFavoriteCount() - 1);
                songService.updateById(song);
            }
        }
    }

    /**
     * 检查是否已收藏（只看active状态）
     * 
     * @param userId 用户ID
     * @param songId 曲谱ID
     * @return 是否已收藏
     */
    public boolean isFavorite(Long userId, Long songId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(FAVORITE.USER_ID.eq(userId))
                .and(FAVORITE.SONG_ID.eq(songId))
                .and(FAVORITE.STATUS.eq("active"));
        
        return favoriteMapper.selectCountByQuery(wrapper) > 0;
    }

    /**
     * 获取用户收藏的曲谱ID列表（只看active状态）
     * 
     * @param userId 用户ID
     * @return 曲谱ID列表
     */
    public List<Long> getFavoriteSongIds(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(FAVORITE.USER_ID.eq(userId))
                .and(FAVORITE.STATUS.eq("active"));
        
        return favoriteMapper.selectListByQuery(wrapper).stream()
                .map(Favorite::getSongId)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户收藏的曲谱详情列表（只看active状态）
     * 
     * @param userId 用户ID
     * @return 曲谱列表
     */
    public List<Song> getFavoriteSongs(Long userId) {
        List<Long> songIds = getFavoriteSongIds(userId);
        if (songIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return songService.listByIds(songIds);
    }

    /**
     * 获取用户收藏数量（只看active状态）
     * 
     * @param userId 用户ID
     * @return 收藏数量
     */
    public long getCount(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(FAVORITE.USER_ID.eq(userId))
                .and(FAVORITE.STATUS.eq("active"));
        
        return favoriteMapper.selectCountByQuery(wrapper);
    }

    /**
     * 获取曲谱的收藏用户列表（active状态）
     * 
     * @param songId 曲谱ID
     * @return 用户列表
     */
    public List<User> getFavoriteUsers(Long songId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(FAVORITE.SONG_ID.eq(songId))
                .and(FAVORITE.STATUS.eq("active"))
                .orderBy(FAVORITE.CREATED_AT.desc());
        
        List<Favorite> favorites = favoriteMapper.selectListByQuery(wrapper);
        List<Long> userIds = favorites.stream().map(Favorite::getUserId).collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return userService.listByIds(userIds);
    }
}