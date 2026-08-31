package com.score.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.score.dto.PageResult;
import com.score.entity.Knowledge;
import com.score.entity.table.KnowledgeTableDef;
import com.score.mapper.KnowledgeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识服务
 * 
 * @author Score Team
 * @since 1.0.0
 */
@Service
public class KnowledgeService {

    private static final KnowledgeTableDef KNOWLEDGE = KnowledgeTableDef.KNOWLEDGE;

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    /**
     * 根据分类获取知识列表
     * 
     * @param category 分类（introduction/history/brand/fingering）
     * @return 知识列表
     */
    public List<Knowledge> listByCategory(String category) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(KNOWLEDGE.CATEGORY.eq(category))
                .orderBy(KNOWLEDGE.SORT_ORDER.asc());
        
        return knowledgeMapper.selectListByQuery(wrapper);
    }

    /**
     * 获取所有知识分类
     * 
     * @return 分类列表
     */
    public List<String> listCategories() {
        return List.of("introduction", "history", "brand", "fingering");
    }

    /**
     * 管理端知识分页列表
     *
     * @param category 分类（可选）
     * @param page     页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    public PageResult<Knowledge> adminPageList(String category, int page, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(KNOWLEDGE.DELETED.eq(0));
        if (category != null && !category.isEmpty()) {
            wrapper.and(KNOWLEDGE.CATEGORY.eq(category));
        }
        wrapper.orderBy(KNOWLEDGE.CATEGORY.asc(), KNOWLEDGE.SORT_ORDER.asc());
        Page<Knowledge> result = knowledgeMapper.paginate(Page.of(page, pageSize), wrapper);
        return PageResult.of(result.getRecords(), result.getTotalRow(), page, pageSize);
    }

    /**
     * 新增知识
     */
    public void addKnowledge(Knowledge knowledge) {
        knowledge.setCreatedAt(LocalDateTime.now());
        knowledge.setUpdatedAt(LocalDateTime.now());
        if (knowledge.getDeleted() == null) knowledge.setDeleted(0);
        if (knowledge.getSortOrder() == null) knowledge.setSortOrder(0);
        knowledgeMapper.insert(knowledge);
    }

    /**
     * 更新知识
     */
    public void updateKnowledge(Knowledge knowledge) {
        knowledge.setUpdatedAt(LocalDateTime.now());
        knowledgeMapper.update(knowledge);
    }

    /**
     * 删除知识（逻辑删除）
     */
    public void deleteKnowledge(Long id) {
        Knowledge knowledge = knowledgeMapper.selectOneById(id);
        if (knowledge != null) {
            knowledge.setDeleted(1);
            knowledge.setUpdatedAt(LocalDateTime.now());
            knowledgeMapper.update(knowledge);
        }
    }

    /**
     * 统计知识数量
     */
    public long count() {
        return knowledgeMapper.selectCountByQuery(
                QueryWrapper.create().where(KNOWLEDGE.DELETED.eq(0))
        );
    }
}