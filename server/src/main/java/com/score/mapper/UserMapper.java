package com.score.mapper;

import com.mybatisflex.core.BaseMapper;
import com.score.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
