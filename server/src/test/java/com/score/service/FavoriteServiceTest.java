package com.score.service;

import com.score.entity.Favorite;
import com.score.entity.Song;
import com.score.mapper.FavoriteMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FavoriteService 单元测试
 *
 * @author Score Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private SongService songService;

    @InjectMocks
    private FavoriteService favoriteService;

    private Long userId;
    private Long songId;
    private Song testSong;

    @BeforeEach
    void setUp() {
        userId = 1L;
        songId = 1L;
        testSong = new Song();
        testSong.setId(songId);
        testSong.setTitle("天空之城");
        testSong.setFavoriteCount(5);
    }

    @Test
    void addFavorite_shouldInsert_whenNotFavorited() {
        when(favoriteMapper.selectCountByQuery(any())).thenReturn(0L);
        when(songService.getById(songId)).thenReturn(testSong);
        when(favoriteMapper.insert(any(Favorite.class))).thenReturn(1);

        favoriteService.addFavorite(userId, songId);

        verify(favoriteMapper).insert(any(Favorite.class));
    }

    @Test
    void addFavorite_shouldNotInsert_whenAlreadyFavorited() {
        when(favoriteMapper.selectCountByQuery(any())).thenReturn(1L);

        favoriteService.addFavorite(userId, songId);

        verify(favoriteMapper, never()).insert(any(Favorite.class));
    }

    @Test
    void removeFavorite_shouldDelete() {
        when(favoriteMapper.deleteByQuery(any())).thenReturn(1);
        when(songService.getById(songId)).thenReturn(testSong);

        favoriteService.removeFavorite(userId, songId);

        verify(favoriteMapper).deleteByQuery(any());
    }

    @Test
    void isFavorite_shouldReturnTrue_whenFavorited() {
        when(favoriteMapper.selectCountByQuery(any())).thenReturn(1L);

        boolean result = favoriteService.isFavorite(userId, songId);

        assertTrue(result);
    }

    @Test
    void isFavorite_shouldReturnFalse_whenNotFavorited() {
        when(favoriteMapper.selectCountByQuery(any())).thenReturn(0L);

        boolean result = favoriteService.isFavorite(userId, songId);

        assertFalse(result);
    }

    @Test
    void getFavoriteSongIds_shouldReturnList() {
        when(favoriteMapper.selectListByQuery(any())).thenReturn(List.of());

        List<Long> result = favoriteService.getFavoriteSongIds(userId);

        assertNotNull(result);
    }
}