package com.score.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.score.dto.PageResult;
import com.score.entity.Banner;
import com.score.entity.table.BannerTableDef;
import com.score.mapper.BannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Banner服务
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class BannerService {

    private static final BannerTableDef BANNER = BannerTableDef.BANNER;

    @Autowired
    private BannerMapper bannerMapper;

    /**
     * 获取启用的Banner列表
     * 
     * @return Banner列表
     */
    public List<Banner> listActive() {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(BANNER.STATUS.eq(1))
                .orderBy(BANNER.SORT_ORDER.asc());
        
        return bannerMapper.selectListByQuery(wrapper);
    }

    /**
     * 管理端Banner列表（含全部状态）
     */
    public List<Banner> adminList() {
        QueryWrapper wrapper = QueryWrapper.create()
                .orderBy(BANNER.SORT_ORDER.asc());
        return bannerMapper.selectListByQuery(wrapper);
    }

    /**
     * 管理端Banner分页列表
     */
    public PageResult<Banner> adminPageList(int page, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create()
                .orderBy(BANNER.SORT_ORDER.asc());
        Page<Banner> result = bannerMapper.paginate(Page.of(page, pageSize), wrapper);
        return PageResult.of(result.getRecords(), result.getTotalRow(), page, pageSize);
    }

    /**
     * 新增Banner
     */
    public void addBanner(Banner banner) {
        banner.setCreatedAt(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());
        if (banner.getStatus() == null) banner.setStatus(1);
        if (banner.getDeleted() == null) banner.setDeleted(0);
        if (banner.getSortOrder() == null) banner.setSortOrder(0);
        bannerMapper.insert(banner);
    }

    /**
     * 更新Banner
     */
    public void updateBanner(Banner banner) {
        banner.setUpdatedAt(LocalDateTime.now());
        bannerMapper.update(banner);
    }

    /**
     * 删除Banner（逻辑删除）
     */
    public void deleteBanner(Long id) {
        Banner banner = bannerMapper.selectOneById(id);
        if (banner != null) {
            banner.setDeleted(1);
            banner.setUpdatedAt(LocalDateTime.now());
            bannerMapper.update(banner);
        }
    }

    /**
     * 统计Banner数量
     */
    public long count() {
        return bannerMapper.selectCountByQuery(QueryWrapper.create());
    }
}