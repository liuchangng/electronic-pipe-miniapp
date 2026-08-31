package com.score.mapper;

import com.mybatisflex.core.BaseMapper;
import com.score.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 反馈Mapper
 *
 * @author Score Team
 * @since 1.0.0
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
}