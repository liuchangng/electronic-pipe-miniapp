package com.score.mapper;

import com.mybatisflex.core.BaseMapper;
import com.score.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员Mapper
 *
 * @author Score Team
 * @since 1.0.0
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}