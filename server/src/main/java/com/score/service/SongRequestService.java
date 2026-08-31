package com.score.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.score.dto.PageResult;
import com.score.entity.SongRequest;
import com.score.entity.table.SongRequestTableDef;
import com.score.mapper.SongRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 求谱申请服务
 *
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class SongRequestService {

    private static final SongRequestTableDef SR = SongRequestTableDef.SONG_REQUEST;

    @Autowired
    private SongRequestMapper songRequestMapper;

    /**
     * 提交求谱申请
     *
     * @param userId      用户ID
     * @param songName    曲谱名称
     * @param artist      原曲作者
     * @param category    分类
     * @param description 补充说明
     * @return 求谱记录
     */
    public SongRequest submitRequest(Long userId, String songName, String artist, String category, String description) {
        SongRequest request = SongRequest.builder()
                .userId(userId)
                .songName(songName)
                .artist(artist)
                .category(category)
                .description(description)
                .status("pending")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(0)
                .build();
        songRequestMapper.insert(request);
        return request;
    }

    /**
     * 获取用户的求谱列表
     *
     * @param userId 用户ID
     * @return 求谱列表
     */
    public List<SongRequest> getUserRequests(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SR.USER_ID.eq(userId))
                .and(SR.DELETED.eq(0))
                .orderBy(SR.CREATED_AT.desc());
        return songRequestMapper.selectListByQuery(wrapper);
    }

    /**
     * 管理员获取所有求谱（支持状态筛选，分页）
     *
     * @param status 状态筛选（可选）
     * @param page   页码
     * @param size   每页数量
     * @return 分页结果
     */
    public PageResult<SongRequest> getAllRequests(String status, int page, int size) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SR.DELETED.eq(0));

        if (status != null && !status.isEmpty()) {
            wrapper.and(SR.STATUS.eq(status));
        }

        wrapper.orderBy(SR.CREATED_AT.desc());

        long total = songRequestMapper.selectCountByQuery(wrapper);

        wrapper.limit(size).offset((long) (page - 1) * size);
        List<SongRequest> list = songRequestMapper.selectListByQuery(wrapper);

        return PageResult.of(list, total, page, size);
    }

    /**
     * 管理员处理求谱
     *
     * @param id     求谱ID
     * @param status 处理状态（accepted/rejected）
     * @param reply  回复内容
     */
    public void handleRequest(Long id, String status, String reply) {
        SongRequest request = songRequestMapper.selectOneById(id);
        if (request == null) {
            throw new RuntimeException("求谱记录不存在");
        }
        request.setStatus(status);
        request.setReply(reply);
        request.setUpdatedAt(LocalDateTime.now());
        songRequestMapper.update(request);
    }

    /**
     * 获取求谱统计数量
     *
     * @param status 状态
     * @return 数量
     */
    public long countByStatus(String status) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SR.DELETED.eq(0))
                .and(SR.STATUS.eq(status));
        return songRequestMapper.selectCountByQuery(wrapper);
    }
}