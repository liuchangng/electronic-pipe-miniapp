package com.score.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.score.common.R;
import com.score.entity.Comment;
import com.score.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评论控制器
 *
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 添加评论（需登录）
     */
    @PostMapping("/add")
    public R<Comment> addComment(@RequestBody Map<String, Object> request) {
        long userId = StpUtil.getLoginIdAsLong();
        Long videoId = Long.valueOf(request.get("videoId").toString());
        String content = (String) request.get("content");
        Long parentId = request.get("parentId") != null ?
                Long.valueOf(request.get("parentId").toString()) : null;

        if (videoId == null || content == null || content.trim().isEmpty()) {
            return R.fail("视频ID和评论内容不能为空");
        }
        if (content.length() > 500) {
            return R.fail("评论内容不能超过500字");
        }

        Comment comment = commentService.addComment(userId, videoId, content.trim(), parentId);
        return R.ok(comment);
    }

    /**
     * 获取视频评论列表（无需登录）
     */
    @GetMapping("/list")
    public R<List<Comment>> getComments(@RequestParam Long videoId) {
        List<Comment> comments = commentService.getVideoComments(videoId);
        return R.ok(comments);
    }

    /**
     * 获取视频评论数量（无需登录）
     */
    @GetMapping("/count")
    public R<Map<String, Object>> getCommentCount(@RequestParam Long videoId) {
        long count = commentService.getCommentCount(videoId);
        return R.ok(Map.of("count", count));
    }

    /**
     * 删除自己的评论（需登录）
     */
    @PostMapping("/delete")
    public R deleteComment(@RequestBody Map<String, Long> request) {
        long userId = StpUtil.getLoginIdAsLong();
        Long commentId = request.get("commentId");
        if (commentId == null) {
            return R.fail("评论ID不能为空");
        }
        commentService.deleteComment(commentId, userId);
        return R.ok("删除成功");
    }
}