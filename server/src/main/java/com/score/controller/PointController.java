package com.score.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.score.common.R;
import com.score.entity.PointLog;
import com.score.entity.User;
import com.score.service.PointService;
import com.score.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分控制器
 *
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/point")
public class PointController {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户积分信息（需登录）
     */
    @GetMapping("/info")
    public R<Map<String, Object>> getPointInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null) {
            return R.fail("用户不存在");
        }

        int point = user.getPoint() != null ? user.getPoint() : 0;
        int level = user.getLevel() != null ? user.getLevel() : 1;
        int nextLevelPoint = PointService.getNextLevelPoint(level);

        Map<String, Object> result = new HashMap<>();
        result.put("point", point);
        result.put("level", level);
        result.put("levelName", PointService.getLevelName(level));
        result.put("nextLevelPoint", nextLevelPoint);
        result.put("nextLevelName", nextLevelPoint > 0 ? PointService.getLevelName(level + 1) : null);
        return R.ok(result);
    }

    /**
     * 每日签到（需登录）
     */
    @PostMapping("/checkin")
    public R<Map<String, Object>> checkIn() {
        long userId = StpUtil.getLoginIdAsLong();
        int earned = pointService.addDailyLoginPoint(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("earned", earned);
        result.put("message", earned > 0 ? "签到成功，+" + earned + "积分" : "今日已签到");
        return R.ok(result);
    }

    /**
     * 获取积分流水（需登录）
     */
    @GetMapping("/logs")
    public R<List<PointLog>> getPointLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        long userId = StpUtil.getLoginIdAsLong();
        List<PointLog> logs = pointService.getUserPointLogs(userId, page, size);
        return R.ok(logs);
    }
}