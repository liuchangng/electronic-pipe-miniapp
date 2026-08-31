package com.score.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.score.common.R;
import com.score.entity.Feedback;
import com.score.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 反馈控制器
 *
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /**
     * 提交反馈
     *
     * @param request 包含 type/content/contact
     * @return 操作结果
     */
    @PostMapping("/submit")
    public R<String> submitFeedback(@RequestBody Map<String, String> request) {
        long userId = StpUtil.getLoginIdAsLong();
        String type = request.getOrDefault("type", "suggestion");
        String content = request.get("content");
        String contact = request.get("contact");
        if (content == null || content.trim().isEmpty()) {
            return R.fail("反馈内容不能为空");
        }
        feedbackService.submitFeedback(userId, type, content, contact);
        return R.ok("提交成功");
    }

    /**
     * 获取当前用户的反馈列表
     *
     * @return 反馈列表
     */
    @GetMapping("/my")
    public R<List<Feedback>> getMyFeedbacks() {
        long userId = StpUtil.getLoginIdAsLong();
        List<Feedback> feedbacks = feedbackService.getUserFeedbacks(userId);
        return R.ok(feedbacks);
    }

    /**
     * 获取所有反馈列表（管理员）
     *
     * @return 反馈列表
     */
    @GetMapping("/list")
    public R<List<Feedback>> getAllFeedbacks() {
        List<Feedback> feedbacks = feedbackService.getAllFeedbacks();
        return R.ok(feedbacks);
    }

    /**
     * 回复反馈（管理员）
     *
     * @param request 包含 id/reply
     * @return 操作结果
     */
    @PostMapping("/reply")
    public R<String> replyFeedback(@RequestBody Map<String, Object> request) {
        Long id = Long.valueOf(request.get("id").toString());
        String reply = (String) request.get("reply");
        feedbackService.replyFeedback(id, reply);
        return R.ok("回复成功");
    }
}