package com.score.mapper;

import com.mybatisflex.core.BaseMapper;
import com.score.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论Mapper
 *
 * @author Score Team
 * @since 1.0.0
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}