package com.score.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.score.entity.Comment;
import com.score.entity.User;
import com.score.entity.table.CommentTableDef;
import com.score.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 评论服务
 *
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class CommentService {

    private static final CommentTableDef C = CommentTableDef.COMMENT;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PointService pointService;

    @Autowired
    private VideoService videoService;

    /**
     * 添加评论（默认approved，后台可审核）
     */
    public Comment addComment(Long userId, Long videoId, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setVideoId(videoId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setStatus("approved"); // 默认通过，后台可改为审核模式
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.insert(comment);

        // 积分：评论+3
        pointService.addCommentPoint(userId);

        // 更新视频评论数
        videoService.incrementCommentCount(videoId);

        // 填充用户信息
        fillUserInfo(comment);
        return comment;
    }

    /**
     * 获取视频的评论列表（只返回approved的，树形结构）
     */
    public List<Comment> getVideoComments(Long videoId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(C.VIDEO_ID.eq(videoId))
                .and(C.STATUS.eq("approved"))
                .orderBy(C.CREATED_AT.asc());
        List<Comment> comments = commentMapper.selectListByQuery(wrapper);
        fillUserInfo(comments);
        return buildCommentTree(comments);
    }

    /**
     * 构建评论树形结构
     * 顶层评论按时间正序，子评论（回复）挂在对应父评论下
     */
    private List<Comment> buildCommentTree(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) return comments;

        // 按parentId分组
        Map<Long, List<Comment>> childrenMap = comments.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Comment::getParentId));

        // 设置children，只返回顶层评论（parentId为null的）
        List<Comment> roots = new ArrayList<>();
        for (Comment c : comments) {
            c.setChildren(childrenMap.getOrDefault(c.getId(), new ArrayList<>()));
            if (c.getParentId() == null) {
                roots.add(c);
            }
        }
        return roots;
    }

    /**
     * 获取评论数量
     */
    public long getCommentCount(Long videoId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(C.VIDEO_ID.eq(videoId))
                .and(C.STATUS.eq("approved"));
        return commentMapper.selectCountByQuery(wrapper);
    }

    /**
     * 删除评论
     */
    public void deleteComment(Long commentId, Long userId) {
        // 先查询评论获取videoId
        Comment comment = commentMapper.selectOneById(commentId);
        if (comment != null && comment.getUserId().equals(userId)) {
            commentMapper.deleteById(commentId);
            // 更新视频评论数
            videoService.decrementCommentCount(comment.getVideoId());
        }
    }

    // ==================== 管理后台 ====================

    /**
     * 获取所有评论（管理后台，分页）
     */
    public List<Comment> getAllComments(String status, int page, int size) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (status != null && !status.isEmpty()) {
            wrapper.and(C.STATUS.eq(status));
        }
        wrapper.orderBy(C.CREATED_AT.desc());
        wrapper.limit((page - 1) * size, size);
        List<Comment> comments = commentMapper.selectListByQuery(wrapper);
        fillUserInfo(comments);
        return comments;
    }

    /**
     * 获取评论总数（管理后台）
     */
    public long getAllCommentCount(String status) {
        QueryWrapper wrapper = QueryWrapper.create();
        if (status != null && !status.isEmpty()) {
            wrapper.and(C.STATUS.eq(status));
        }
        return commentMapper.selectCountByQuery(wrapper);
    }

    /**
     * 审核评论（通过/拒绝）
     */
    public void reviewComment(Long commentId, String status) {
        Comment comment = commentMapper.selectOneById(commentId);
        if (comment != null) {
            comment.setStatus(status);
            comment.setUpdatedAt(LocalDateTime.now());
            commentMapper.update(comment);
        }
    }

    /**
     * 管理员删除评论
     */
    public void adminDeleteComment(Long commentId) {
        commentMapper.deleteById(commentId);
    }

    // ==================== 私有方法 ====================

    private void fillUserInfo(Comment comment) {
        if (comment == null) return;
        try {
            User user = userService.getById(comment.getUserId());
            if (user != null) {
                comment.setNickname(user.getNickname());
                comment.setAvatar(user.getAvatar());
            }
        } catch (Exception e) {
            // 忽略
        }
    }

    private void fillUserInfo(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) return;
        // 批量获取用户信息
        List<Long> userIds = comments.stream().map(Comment::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userIds.stream()
                .map(id -> {
                    try { return userService.getById(id); } catch (Exception e) { return null; }
                })
                .filter(u -> u != null)
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        comments.forEach(c -> {
            User user = userMap.get(c.getUserId());
            if (user != null) {
                c.setNickname(user.getNickname());
                c.setAvatar(user.getAvatar());
            }
        });
    }
}