package com.score.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.score.entity.Feedback;
import com.score.entity.table.FeedbackTableDef;
import com.score.mapper.FeedbackMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 反馈服务
 *
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class FeedbackService {

    private static final FeedbackTableDef FEEDBACK = FeedbackTableDef.FEEDBACK;

    @Autowired
    private FeedbackMapper feedbackMapper;

    /**
     * 提交反馈
     */
    public void submitFeedback(Long userId, String type, String content, String contact) {
        Feedback feedback = Feedback.builder()
                .userId(userId)
                .type(type)
                .content(content)
                .contact(contact)
                .status("pending")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        feedbackMapper.insert(feedback);
    }

    /**
     * 获取用户的反馈列表
     */
    public List<Feedback> getUserFeedbacks(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(FEEDBACK.USER_ID.eq(userId))
                .orderBy(FEEDBACK.CREATED_AT.desc());
        return feedbackMapper.selectListByQuery(wrapper);
    }

    /**
     * 获取所有反馈列表（管理员）
     */
    public List<Feedback> getAllFeedbacks() {
        QueryWrapper wrapper = QueryWrapper.create()
                .orderBy(FEEDBACK.CREATED_AT.desc());
        return feedbackMapper.selectListByQuery(wrapper);
    }

    /**
     * 回复反馈（管理员）
     */
    public void replyFeedback(Long id, String reply) {
        Feedback feedback = feedbackMapper.selectOneById(id);
        if (feedback != null) {
            feedback.setReply(reply);
            feedback.setStatus("resolved");
            feedback.setUpdatedAt(LocalDateTime.now());
            feedbackMapper.update(feedback);
        }
    }
}