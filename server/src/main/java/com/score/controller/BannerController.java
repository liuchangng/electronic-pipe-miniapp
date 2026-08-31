package com.score.controller;

import com.score.common.R;
import com.score.entity.Banner;
import com.score.service.BannerService;
import com.score.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Banner控制器
 * 
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private StorageService storageService;

    /**
     * 获取Banner列表
     * 
     * @return Banner列表
     */
    @GetMapping("/list")
    public R<List<Banner>> getBannerList() {
        List<Banner> banners = bannerService.listActive();
        storageService.fillBannerUrls(banners);
        return R.ok(banners);
    }
}
