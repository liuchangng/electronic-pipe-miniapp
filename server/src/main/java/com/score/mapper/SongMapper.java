package com.score.mapper;

import com.mybatisflex.core.BaseMapper;
import com.score.entity.Song;
import org.apache.ibatis.annotations.Mapper;

/**
 * 曲谱Mapper
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Mapper
public interface SongMapper extends BaseMapper<Song> {
}
