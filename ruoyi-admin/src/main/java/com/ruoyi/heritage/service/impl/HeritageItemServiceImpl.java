package com.ruoyi.heritage.service.impl;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.heritage.domain.*;
import com.ruoyi.heritage.mapper.HeritageUserActionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.heritage.mapper.HeritageItemMapper;
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
    @Autowired
    private RedisCache redisCache; // 若依自带的 Redis 工具类


    /**
     * 增加浏览量
     */
    @Override
    public int addViewCount(Long itemId) {
        // type=1 (浏览), status=1 (增加)
        return heritageItemMapper.updateItemCount(itemId, 1, 1);
    }

    /**
     * 增加点赞数 (简单增加，不走切换逻辑时使用)
     */
    /*@Override
    public int addLikeCount(Long itemId) {
        // type=2 (点赞), status=1 (增加)
        return heritageItemMapper.updateItemCount(itemId, 2, 1);
    }*/

    /**
     * 增加收藏数 (简单增加，不走切换逻辑时使用)
     */
    /*@Override
    public int addFavoriteCount(Long itemId) {
        // type=3 (收藏), status=1 (增加)
        return heritageItemMapper.updateItemCount(itemId, 3, 1);
    }*/

    /**
     * 切换点赞/收藏状态 (长期方案核心逻辑)
     */
    @Override
    @Transactional
    public int toggleAction(Long itemId, Integer type) {
        Long userId = SecurityUtils.getUserId();

        HeritageUserAction query = new HeritageUserAction();
        query.setUserId(userId);
        query.setItemId(itemId);
        query.setActionType(type); // 前端传来的 type 必须是 2(点赞) 或 3(收藏)

        int count = actionMapper.selectCountByAction(query);

        if (count == 0) {
            // 还没点过 -> 插入记录，数量 +1
            actionMapper.insertAction(query);
            // 操作成功后，清除大屏缓存，强制下次查询走数据库
            redisCache.deleteObject("heritage:stats:dashboard");
            return heritageItemMapper.updateItemCount(itemId, type, 1);
        } else {
            // 已经点过 -> 删除记录，数量 -1
            actionMapper.deleteAction(query);
            // 操作成功后，清除大屏缓存，强制下次查询走数据库
            redisCache.deleteObject("heritage:stats:dashboard");
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

        // 执行新增操作
        int result = heritageItemMapper.insertHeritageItem(heritageItem);

        if (result > 0) {
            redisCache.deleteObject("heritage:stats:dashboard");
        }

        return result;
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
        // 执行修改操作
        int result = heritageItemMapper.updateHeritageItem(heritageItem);
        if (result > 0) {
            redisCache.deleteObject("heritage:stats:dashboard");
        }
        return result;
    }

    /**
     * 批量删除非遗展品
     *
     * @param itemIds 需要删除的非遗展品主键
     * @return 结果
     */
    @Override
    public int deleteHeritageItemByItemIds(Long[] itemIds) {
        // 执行批量删除操作
        int result = heritageItemMapper.deleteHeritageItemByItemIds(itemIds);
        if (result > 0) {
            redisCache.deleteObject("heritage:stats:dashboard");
        }

        return result;
    }

    /**
     * 删除非遗展品信息
     *
     * @param itemId 非遗展品主键
     * @return 结果
     */
    @Override
    public int deleteHeritageItemByItemId(Long itemId) {
        // 执行删除操作
        int result = heritageItemMapper.deleteHeritageItemByItemId(itemId);

        if (result > 0) {
            redisCache.deleteObject("heritage:stats:dashboard");
        }
        return result;
    }

    /**
     * 查询当前登录用户的收藏列表
     */
    @Override
    public List<HeritageItem> selectUserCollectionList() {
        // 获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        return heritageItemMapper.selectUserCollectionList(userId);
    }

    @Override
    public List<HeritageItem> selectUserPublishList(HeritageItem heritageItem) {
        return heritageItemMapper.selectMyHeritageItemList(heritageItem);
    }

    /**
     * 实现：获取首页驾驶舱统计数据
     */
    @Override
    public IndexStatsVo getDashboardData() {
        // 1. 定义缓存的 Key
        String cacheKey = "heritage:stats:dashboard";

        // 2. 从 Redis 中获取数据
        IndexStatsVo cachedStats = redisCache.getCacheObject(cacheKey);

        // 3. 判断缓存是否存在
        if (cachedStats != null) {
            // 如果缓存命中了，直接返回，不走后面的数据库查询
            return cachedStats;
        }

        // 4. 如果缓存不存在，执行原本的数据库查询逻辑
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

        // 2. 分类饼图 (现在只存大类数据)
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

        // 5. 将查询出的结果存入 Redis，并设置过期时间（例如：10分钟）
        // 建议不要设太长，保证大屏数据有准实时性
        redisCache.setCacheObject(cacheKey, stats, 10, TimeUnit.MINUTES);

        return stats;
    }

}
