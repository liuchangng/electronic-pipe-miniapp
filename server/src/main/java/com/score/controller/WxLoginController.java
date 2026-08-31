package com.score.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.score.common.R;
import com.score.entity.User;
import com.score.service.UserService;
import com.score.service.StorageService;
import com.score.service.WxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信登录控制器
 * 
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/user")
public class WxLoginController {

    @Autowired
    private WxService wxService;

    @Autowired
    private UserService userService;

    @Autowired
    private StorageService storageService;

    /**
     * 微信小程序登录
     * 
     * @param request 登录请求（code: 微信登录凭证）
     * @return Token 和用户信息
     */
    @PostMapping("/login")
    public R login(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        if (code == null || code.isEmpty()) {
            return R.fail("登录凭证不能为空");
        }

        // 调用微信接口获取 openid
        String openid = wxService.getOpenid(code);
        if (openid == null || openid.isEmpty()) {
            return R.fail("微信登录失败，请检查网络或AppID配置");
        }

        // 查找或创建用户
        User user = userService.findOrCreateByOpenid(openid);

        // Sa-Token 登录
        StpUtil.login(user.getId());

        // 返回 Token 和用户信息
        Map<String, Object> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());
        storageService.fillUserUrls(user);
        result.put("userInfo", user);

        return R.ok(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public R getUserInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user == null) {
            return R.fail("用户不存在");
        }
        storageService.fillUserUrls(user);
        return R.ok(user);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public R logout() {
        StpUtil.logout();
        return R.ok("退出成功");
    }

    /**
     * 更新用户资料（头像、昵称）
     *
     * @param request 请求体（avatar: 头像URL, nickname: 昵称）
     * @return 更新后的用户信息
     */
    @PostMapping("/updateProfile")
    public R<User> updateProfile(@RequestBody Map<String, String> request) {
        long userId = StpUtil.getLoginIdAsLong();
        String avatar = request.get("avatar");
        String nickname = request.get("nickname");
        userService.updateProfile(userId, avatar, nickname);
        User user = userService.getById(userId);
        storageService.fillUserUrls(user);
        return R.ok(user);
    }
}
