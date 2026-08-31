package com.score.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.score.entity.PointLog;
import com.score.entity.User;
import com.score.entity.table.PointLogTableDef;
import com.score.mapper.PointLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 积分服务
 *
 * 积分规则：
 * - 每日登录：+5分
 * - 浏览曲谱：+1分/首（每日上限10分）
 * - 收藏曲谱/视频：+2分/次
 * - 点赞视频：+1分/次（每日上限5分）
 * - 评论：+3分/条（每日上限15分）
 * - 求谱被采纳：+20分
 *
 * 等级规则：
 * - Lv1 新手：0分
 * - Lv2 学徒：100分
 * - Lv3 乐手：500分
 * - Lv4 大师：2000分
 *
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class PointService {

    private static final PointLogTableDef PL = PointLogTableDef.POINT_LOG;

    // 积分值
    public static final int POINT_DAILY_LOGIN = 5;
    public static final int POINT_VIEW_SONG = 1;
    public static final int POINT_FAVORITE = 2;
    public static final int POINT_LIKE = 1;
    public static final int POINT_COMMENT = 3;
    public static final int POINT_REQUEST_ACCEPTED = 20;

    // 每日上限
    public static final int DAILY_LIMIT_VIEW_SONG = 10;
    public static final int DAILY_LIMIT_LIKE = 5;
    public static final int DAILY_LIMIT_COMMENT = 15;

    // 等级阈值
    public static final int[] LEVEL_THRESHOLDS = {0, 100, 500, 2000};
    public static final String[] LEVEL_NAMES = {"新手", "学徒", "乐手", "大师"};

    @Autowired
    private PointLogMapper pointLogMapper;

    @Autowired
    private UserService userService;

    /**
     * 添加积分
     * @param userId 用户ID
     * @param type 积分类型
     * @param amount 积分值
     * @param description 描述
     * @param dailyLimit 每日上限（0表示不限制）
     * @return 实际获得的积分数（0表示已达上限）
     */
    public int addPoint(Long userId, String type, int amount, String description, int dailyLimit) {
        // 检查每日上限
        if (dailyLimit > 0) {
            int todayEarned = getTodayEarned(userId, type);
            if (todayEarned >= dailyLimit) {
                return 0; // 已达上限
            }
            // 调整本次可获得积分
            amount = Math.min(amount, dailyLimit - todayEarned);
        }

        // 记录流水
        PointLog log = new PointLog();
        log.setUserId(userId);
        log.setType(type);
        log.setAmount(amount);
        log.setDescription(description);
        log.setCreatedAt(LocalDateTime.now());
        pointLogMapper.insert(log);

        // 更新用户积分和等级
        User user = userService.getById(userId);
        if (user != null) {
            int newPoint = (user.getPoint() != null ? user.getPoint() : 0) + amount;
            int newLevel = calculateLevel(newPoint);
            user.setPoint(newPoint);
            user.setLevel(newLevel);
            userService.updateById(user);
        }

        return amount;
    }

    /**
     * 获取今日已获积分
     */
    private int getTodayEarned(Long userId, String type) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        QueryWrapper wrapper = QueryWrapper.create()
                .where(PL.USER_ID.eq(userId))
                .and(PL.TYPE.eq(type))
                .and(PL.CREATED_AT.ge(startOfDay))
                .and(PL.CREATED_AT.le(endOfDay));

        List<PointLog> logs = pointLogMapper.selectListByQuery(wrapper);
        return logs.stream().mapToInt(PointLog::getAmount).sum();
    }

    /**
     * 根据积分计算等级
     */
    public static int calculateLevel(int point) {
        for (int i = LEVEL_THRESHOLDS.length - 1; i >= 0; i--) {
            if (point >= LEVEL_THRESHOLDS[i]) {
                return i + 1;
            }
        }
        return 1;
    }

    /**
     * 获取等级名称
     */
    public static String getLevelName(int level) {
        if (level >= 1 && level <= LEVEL_NAMES.length) {
            return "Lv" + level + " " + LEVEL_NAMES[level - 1];
        }
        return "Lv1 新手";
    }

    /**
     * 获取下一等级所需积分
     */
    public static int getNextLevelPoint(int currentLevel) {
        if (currentLevel < LEVEL_THRESHOLDS.length) {
            return LEVEL_THRESHOLDS[currentLevel];
        }
        return -1; // 已满级
    }

    /**
     * 获取用户积分流水（分页）
     */
    public List<PointLog> getUserPointLogs(Long userId, int page, int size) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(PL.USER_ID.eq(userId))
                .orderBy(PL.CREATED_AT.desc())
                .limit((page - 1) * size, size);
        return pointLogMapper.selectListByQuery(wrapper);
    }

    /**
     * 获取用户积分流水总数
     */
    public long getUserPointLogCount(Long userId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(PL.USER_ID.eq(userId));
        return pointLogMapper.selectCountByQuery(wrapper);
    }

    // ==================== 便捷方法 ====================

    /** 每日登录积分 */
    public int addDailyLoginPoint(Long userId) {
        return addPoint(userId, "daily_login", POINT_DAILY_LOGIN, "每日登录", 0);
    }

    /** 浏览曲谱积分 */
    public int addViewSongPoint(Long userId) {
        return addPoint(userId, "view_song", POINT_VIEW_SONG, "浏览曲谱", DAILY_LIMIT_VIEW_SONG);
    }

    /** 收藏积分 */
    public int addFavoritePoint(Long userId) {
        return addPoint(userId, "favorite", POINT_FAVORITE, "收藏", 0);
    }

    /** 点赞积分 */
    public int addLikePoint(Long userId) {
        return addPoint(userId, "like", POINT_LIKE, "点赞", DAILY_LIMIT_LIKE);
    }

    /** 评论积分 */
    public int addCommentPoint(Long userId) {
        return addPoint(userId, "comment", POINT_COMMENT, "评论", DAILY_LIMIT_COMMENT);
    }

    /** 求谱被采纳积分 */
    public int addRequestAcceptedPoint(Long userId) {
        return addPoint(userId, "request_accepted", POINT_REQUEST_ACCEPTED, "求谱被采纳", 0);
    }
}