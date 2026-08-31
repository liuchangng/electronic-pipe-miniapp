package com.score.mapper;

import com.mybatisflex.core.BaseMapper;
import com.score.entity.VideoShare;
import org.apache.ibatis.annotations.Mapper;

/**
 * 视频分享记录Mapper
 *
 * @author Score Team
 * @since 1.0.0
 */
@Mapper
public interface VideoShareMapper extends BaseMapper<VideoShare> {
}