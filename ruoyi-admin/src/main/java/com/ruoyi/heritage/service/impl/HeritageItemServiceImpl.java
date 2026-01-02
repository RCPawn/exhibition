package com.ruoyi.heritage.service.impl;

import java.util.List;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.heritage.domain.HeritageUserAction;
import com.ruoyi.heritage.domain.IndexStatsVo;
import com.ruoyi.heritage.mapper.HeritageUserActionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.heritage.mapper.HeritageItemMapper;
import com.ruoyi.heritage.domain.HeritageItem;
import com.ruoyi.heritage.service.IHeritageItemService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 非遗展品Service业务层处理
 *
 * @author ruoyi
 * @date 2025-12-25
 */
@Service
public class HeritageItemServiceImpl implements IHeritageItemService {
    @Autowired
    private HeritageItemMapper heritageItemMapper;
    @Autowired
    private HeritageUserActionMapper actionMapper;

    // 在实现类中调用 mapper
    @Override
    public int addViewCount(Long itemId) {
        return heritageItemMapper.incrementViewCount(itemId);
    }

    @Override
    public int addLikeCount(Long itemId) {
        return heritageItemMapper.incrementLikeCount(itemId);
    }

    @Override
    public int addFavoriteCount(Long itemId) {
        return heritageItemMapper.incrementFavoriteCount(itemId);
    }

    /**
     * 切换点赞/收藏状态 (核心业务逻辑)
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 报错自动回滚，保证数据一致性
    public int toggleAction(Long itemId, Integer type) {
        // 1. 获取当前登录用户的ID (若依内置工具类)
        Long userId = SecurityUtils.getUserId();

        // 2. 构造查询条件
        HeritageUserAction query = new HeritageUserAction();
        query.setUserId(userId);
        query.setItemId(itemId);
        query.setActionType(type);

        // 3. 检查是否已经有点赞/收藏记录
        int count = actionMapper.selectCountByAction(query);

        if (count == 0) {
            // 场景 A：用户还没点过 -> 增加记录，数量 +1
            actionMapper.insertAction(query);
            // 这里 status 传 1
            return heritageItemMapper.updateItemCount(itemId, type, 1);
        } else {
            // 场景 B：用户已经点过了 -> 删除记录，数量 -1
            actionMapper.deleteAction(query);
            // 这里 status 传 0
            return heritageItemMapper.updateItemCount(itemId, type, 0);
        }
    }

    // 还要重写根据ID查询展品的方法，把状态带出来
    @Override
    public HeritageItem selectHeritageItemByItemId(Long itemId) {
        HeritageItem item = heritageItemMapper.selectHeritageItemByItemId(itemId);
        if (item != null) {
            // 获取当前登录用户（若依标准写法）
            Long userId = SecurityUtils.getUserId();
            if (userId != null) {
                // 注意 actionType 要对上：2是点赞，3是收藏
                item.setIsLiked(actionMapper.checkStatus(userId, itemId, 2) > 0);
                item.setIsCollected(actionMapper.checkStatus(userId, itemId, 3) > 0);
            }
        }
        return item;
    }

    /**
     * 查询非遗展品列表
     *
     * @param heritageItem 非遗展品
     * @return 非遗展品
     */
    @Override
    public List<HeritageItem> selectHeritageItemList(HeritageItem heritageItem) {
        return heritageItemMapper.selectHeritageItemList(heritageItem);
    }

    /**
     * 新增非遗展品
     * @param heritageItem 非遗展品
     * @return 结果
     */
    @Override
    public int insertHeritageItem(HeritageItem heritageItem) {
        // 1. 手动设置创建人（从 Security 上下文获取当前登录账号）
        heritageItem.setCreateBy(SecurityUtils.getUsername());

        // 2. 手动设置创建时间（虽然数据库有默认值，但 Java 层设置更安全，防止 MyBatis 传 null 覆盖默认值）
        heritageItem.setCreateTime(DateUtils.getNowDate());

        // 3. 更新时间在新增时通常也要初始化
        heritageItem.setUpdateTime(DateUtils.getNowDate());

        return heritageItemMapper.insertHeritageItem(heritageItem);
    }

    /**
     * 修改非遗展品
     *
     * @param heritageItem 非遗展品
     * @return 结果
     */
    @Override
    public int updateHeritageItem(HeritageItem heritageItem) {
        heritageItem.setUpdateTime(DateUtils.getNowDate());
        return heritageItemMapper.updateHeritageItem(heritageItem);
    }

    /**
     * 批量删除非遗展品
     *
     * @param itemIds 需要删除的非遗展品主键
     * @return 结果
     */
    @Override
    public int deleteHeritageItemByItemIds(Long[] itemIds) {
        return heritageItemMapper.deleteHeritageItemByItemIds(itemIds);
    }

    /**
     * 删除非遗展品信息
     *
     * @param itemId 非遗展品主键
     * @return 结果
     */
    @Override
    public int deleteHeritageItemByItemId(Long itemId) {
        return heritageItemMapper.deleteHeritageItemByItemId(itemId);
    }

    @Override
    public List<HeritageItem> selectUserCollectionList() {
        // 获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        return heritageItemMapper.selectUserCollectionList(userId);
    }

    /**
     * 实现：获取首页驾驶舱统计数据
     */
    @Override
    public IndexStatsVo getDashboardData() {
        IndexStatsVo stats = new IndexStatsVo();

        // 1. 基础指标 (全部改为查数据库)
        // 展品总数
        stats.setTotalItems(heritageItemMapper.selectHeritageItemList(null).size());

        // 浏览总数 (处理空指针)
        Long views = heritageItemMapper.sumViewCount();
        stats.setTotalViews(views != null ? views : 0);

        // --- 互动数 ---
        Long interactions = heritageItemMapper.sumTotalInteractions();
        stats.setTotalInteractions(interactions != null ? interactions : 0);

        // 2. 分类饼图
        stats.setCategoryPie(heritageItemMapper.selectCategoryStats());

        // 3. 中间3D模型 (取最火的那个)
        HeritageItem topItem = heritageItemMapper.selectMostPopularItem();
        if (topItem != null) {
            stats.setCenterModelUrl(topItem.getModelFile());
            stats.setCenterModelName(topItem.getItemName());
        }

        // 4. 各种图表数据
        stats.setResourceComposition(heritageItemMapper.selectResourceStats());
        stats.setInteractionStats(heritageItemMapper.selectInteractionStats());
        stats.setLatestItems(heritageItemMapper.selectLatestItems());
        stats.setWordCloud(heritageItemMapper.selectItemNames());
        stats.setTop5Items(heritageItemMapper.selectTop5Trend());

        return stats;
    }


}
