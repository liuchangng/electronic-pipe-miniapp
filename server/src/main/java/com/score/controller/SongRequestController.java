package com.score.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.score.common.R;
import com.score.entity.SongRequest;
import com.score.service.SongRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 求谱申请控制器
 *
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/song-request")
public class SongRequestController {

    @Autowired
    private SongRequestService songRequestService;

    /**
     * 提交求谱申请（需登录）
     *
     * @param request 请求体（songName, artist, category, description）
     * @return 操作结果
     */
    @PostMapping("/submit")
    public R submitRequest(@RequestBody Map<String, String> request) {
        long userId = StpUtil.getLoginIdAsLong();
        String songName = request.get("songName");
        String artist = request.get("artist");
        String category = request.get("category");
        String description = request.get("description");

        if (songName == null || songName.trim().isEmpty()) {
            return R.fail("曲谱名称不能为空");
        }

        SongRequest result = songRequestService.submitRequest(userId, songName.trim(), artist, category, description);
        return R.ok("提交成功");
    }

    /**
     * 获取我的求谱列表（需登录）
     *
     * @return 求谱列表
     */
    @GetMapping("/my")
    public R<List<SongRequest>> getMyRequests() {
        long userId = StpUtil.getLoginIdAsLong();
        List<SongRequest> list = songRequestService.getUserRequests(userId);
        return R.ok(list);
    }
}