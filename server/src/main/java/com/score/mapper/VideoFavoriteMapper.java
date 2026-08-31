package com.score.mapper;

import com.mybatisflex.core.BaseMapper;
import com.score.entity.VideoFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频收藏Mapper
 *
 * @author Score Team
 * @since 1.0.0
 */
@Mapper
public interface VideoFavoriteMapper extends BaseMapper<VideoFavorite> {
}