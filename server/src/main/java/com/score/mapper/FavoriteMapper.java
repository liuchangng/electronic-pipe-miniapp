package com.score.mapper;

import com.mybatisflex.core.BaseMapper;
import com.score.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏Mapper
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
