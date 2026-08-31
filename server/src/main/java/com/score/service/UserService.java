package com.score.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.score.dto.PageResult;
import com.score.entity.User;
import com.score.entity.table.UserTableDef;
import com.score.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class UserService {

    private static final UserTableDef USER = UserTableDef.USER;

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据openid查找或创建用户
     * 
     * @param openid 微信openid
     * @return 用户信息
     */
    public User findOrCreateByOpenid(String openid) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(USER.OPENID.eq(openid));
        
        User user = userMapper.selectOneByQuery(wrapper);
        
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("");
            user.setAvatar("");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setDeleted(0);
            userMapper.insert(user);
        }
        
        return user;
    }

    /**
     * 更新用户昵称
     * 
     * @param userId 用户ID
     * @param nickname 新昵称
     */
    public void updateNickname(Long userId, String nickname) {
        User user = userMapper.selectOneById(userId);
        if (user != null) {
            user.setNickname(nickname);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);
        }
    }

    /**
     * 更新用户资料（头像、昵称）
     *
     * @param userId 用户ID
     * @param avatar 头像URL
     * @param nickname 昵称
     */
    public void updateProfile(Long userId, String avatar, String nickname) {
        User user = userMapper.selectOneById(userId);
        if (user != null) {
            if (avatar != null && !avatar.isEmpty()) {
                user.setAvatar(avatar);
            }
            if (nickname != null && !nickname.isEmpty()) {
                user.setNickname(nickname);
            }
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);
        }
    }

    /**
     * 根据ID获取用户
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    public User getById(Long userId) {
        return userMapper.selectOneById(userId);
    }

    /**
     * 更新用户信息
     *
     * @param user 用户实体
     */
    public void updateById(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.update(user);
    }

    /**
     * 管理端用户列表
     */
    public List<User> adminList(String keyword) {
        QueryWrapper wrapper = buildAdminQueryWrapper(keyword);
        return userMapper.selectListByQuery(wrapper);
    }

    /**
     * 管理端用户分页列表
     */
    public PageResult<User> adminPageList(String keyword, int page, int pageSize) {
        QueryWrapper wrapper = buildAdminQueryWrapper(keyword);
        Page<User> result = userMapper.paginate(Page.of(page, pageSize), wrapper);
        return PageResult.of(result.getRecords(), result.getTotalRow(), page, pageSize);
    }

    private QueryWrapper buildAdminQueryWrapper(String keyword) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.where(USER.NICKNAME.like("%" + keyword + "%")
                    .or(USER.OPENID.like("%" + keyword + "%")));
        }
        wrapper.orderBy(USER.CREATED_AT.desc());
        return wrapper;
    }

    /**
     * 更新用户状态
     */
    public void updateStatus(Long userId, Integer status) {
        User user = userMapper.selectOneById(userId);
        if (user != null) {
            user.setDeleted(status);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);
        }
    }

    /**
     * 统计用户数量
     */
    public long count() {
        return userMapper.selectCountByQuery(QueryWrapper.create());
    }

    /**
     * 根据ID列表批量查询用户
     */
    public List<User> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        QueryWrapper wrapper = QueryWrapper.create()
                .where(USER.ID.in(ids));
        return userMapper.selectListByQuery(wrapper);
    }
}