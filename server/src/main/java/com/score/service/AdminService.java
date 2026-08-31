package com.score.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.score.entity.Admin;
import com.score.entity.table.AdminTableDef;
import com.score.mapper.AdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 管理员服务
 *
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class AdminService {

    private static final AdminTableDef ADMIN = AdminTableDef.ADMIN;

    @Autowired
    private AdminMapper adminMapper;

    /**
     * 管理员登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @return 管理员信息，null表示登录失败
     */
    public Admin login(String username, String password) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(ADMIN.USERNAME.eq(username))
                .and(ADMIN.PASSWORD.eq(password))
                .and(ADMIN.STATUS.eq(1));
        return adminMapper.selectOneByQuery(wrapper);
    }

    /**
     * 根据ID获取管理员
     */
    public Admin getById(Long id) {
        return adminMapper.selectOneById(id);
    }
}