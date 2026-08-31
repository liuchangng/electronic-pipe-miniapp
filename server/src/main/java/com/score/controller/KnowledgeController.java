package com.score.controller;

import com.score.common.R;
import com.score.entity.Knowledge;
import com.score.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识控制器
 * 
 * @author Score Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeService knowledgeService;

    /**
     * 获取知识列表
     * 
     * @param category 分类（introduction/history/brand/fingering）
     * @return 知识列表
     */
    @GetMapping("/list")
    public R<List<Knowledge>> getKnowledgeList(
            @RequestParam(required = false) String category) {
        
        List<Knowledge> list;
        if (category != null && !category.isEmpty()) {
            list = knowledgeService.listByCategory(category);
        } else {
            // 返回所有分类的知识
            list = new java.util.ArrayList<>();
            for (String cat : knowledgeService.listCategories()) {
                list.addAll(knowledgeService.listByCategory(cat));
            }
        }
        
        return R.ok(list);
    }
}
