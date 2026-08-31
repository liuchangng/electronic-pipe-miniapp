package com.score.service;

import com.score.entity.Song;
import com.score.mapper.SongMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SongService 单元测试
 *
 * @author Score Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SongServiceTest {

    @Mock
    private SongMapper songMapper;

    @InjectMocks
    private SongService songService;

    private Song testSong;

    @BeforeEach
    void setUp() {
        testSong = new Song();
        testSong.setId(1L);
        testSong.setTitle("天空之城");
        testSong.setAuthor("久石让");
        testSong.setColor("#007AFF");
    }

    @Test
    void getById_shouldReturnSong_whenExists() {
        when(songMapper.selectOneById(1L)).thenReturn(testSong);

        Song result = songService.getById(1L);

        assertNotNull(result);
        assertEquals("天空之城", result.getTitle());
        assertEquals("久石让", result.getAuthor());
        verify(songMapper).selectOneById(1L);
    }

    @Test
    void getById_shouldReturnNull_whenNotExists() {
        when(songMapper.selectOneById(999L)).thenReturn(null);

        Song result = songService.getById(999L);

        assertNull(result);
        verify(songMapper).selectOneById(999L);
    }

    @Test
    void searchByKeyword_shouldReturnMatchingSongs() {
        Song song1 = new Song();
        song1.setId(1L);
        song1.setTitle("天空之城");
        Song song2 = new Song();
        song2.setId(2L);
        song2.setTitle("天空之城钢琴版");

        when(songMapper.selectListByQuery(any())).thenReturn(Arrays.asList(song1, song2));

        List<Song> result = songService.searchByKeyword("天空");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(songMapper).selectListByQuery(any());
    }

    @Test
    void searchByKeyword_shouldReturnEmptyList_whenNoMatch() {
        when(songMapper.selectListByQuery(any())).thenReturn(List.of());

        List<Song> result = songService.searchByKeyword("不存在的曲谱");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listHot_shouldReturnSongsOrderedByFavoriteCount() {
        when(songMapper.selectListByQuery(any())).thenReturn(List.of(testSong));

        List<Song> result = songService.listHot();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(songMapper).selectListByQuery(any());
    }

    @Test
    void listNew_shouldReturnSongsOrderedByCreatedAt() {
        when(songMapper.selectListByQuery(any())).thenReturn(List.of(testSong));

        List<Song> result = songService.listNew();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(songMapper).selectListByQuery(any());
    }

    @Test
    void updateById_shouldCallMapper() {
        when(songMapper.update(any(Song.class))).thenReturn(1);

        songService.updateById(testSong);

        verify(songMapper).update(testSong);
    }
}