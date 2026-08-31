package com.score.mapper;

import com.mybatisflex.core.BaseMapper;
import com.score.entity.VideoLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频点赞Mapper
 *
 * @author Score Team
 * @since 1.0.0
 */
@Mapper
public interface VideoLikeMapper extends BaseMapper<VideoLike> {
}