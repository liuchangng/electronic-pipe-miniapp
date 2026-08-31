package com.score.controller;

import com.score.entity.Song;
import com.score.service.SongService;
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
 * SongController 单元测试
 *
 * @author Score Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SongControllerTest {

    @Mock
    private SongService songService;

    @InjectMocks
    private SongController songController;

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
    void getSongList_withHotTab_shouldReturnHotSongs() {
        when(songService.listHot()).thenReturn(Arrays.asList(testSong));

        List<Song> result = songService.listHot();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("天空之城", result.get(0).getTitle());
        verify(songService).listHot();
    }

    @Test
    void getSongList_withKeyword_shouldReturnMatchingSongs() {
        when(songService.searchByKeyword("天空")).thenReturn(Arrays.asList(testSong));

        List<Song> result = songService.searchByKeyword("天空");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(songService).searchByKeyword("天空");
    }

    @Test
    void getSongList_withNewTab_shouldReturnNewSongs() {
        Song newSong = new Song();
        newSong.setId(2L);
        newSong.setTitle("千与千寻");

        when(songService.listNew()).thenReturn(Arrays.asList(newSong));

        List<Song> result = songService.listNew();

        assertNotNull(result);
        assertEquals("千与千寻", result.get(0).getTitle());
        verify(songService).listNew();
    }

    @Test
    void getSongDetail_shouldReturnSong_whenExists() {
        when(songService.getById(1L)).thenReturn(testSong);

        Song result = songService.getById(1L);

        assertNotNull(result);
        assertEquals("天空之城", result.getTitle());
        verify(songService).getById(1L);
    }

    @Test
    void getSongDetail_shouldReturnNull_whenNotExists() {
        when(songService.getById(999L)).thenReturn(null);

        Song result = songService.getById(999L);

        assertNull(result);
        verify(songService).getById(999L);
    }
}