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
 * 非遗展品Service业务层处理 (高性能缓存优化版)
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
    private RedisCache redisCache;

    // --- 常量定义 ---
    private static final String CACHE_KEY_DASHBOARD = "heritage:stats:dashboard";
    private static final String CACHE_KEY_ITEM_DETAIL_PREFIX = "heritage:item:detail:";
    private static final String CACHE_KEY_VIEW_BUFFER = "heritage:view_buffer"; // 使用 Hash 结构存储所有展品的浏览增量

    /**
     * 增加浏览量 (写缓冲模式)
     * 优化：不直接写库，而是利用 Redis 原子递增。
     * 注意：需要配合定时任务将 heritage:view_buffer 的数据同步回 MySQL。
     */
    @Override
    public int addViewCount(Long itemId) {
        // 在 Redis Hash 中，将对应 itemId 的浏览量 +1
        redisCache.incrementCacheMapValue(CACHE_KEY_VIEW_BUFFER, String.valueOf(itemId), 1);
        // 返回 1 表示操作成功 (虽然没写库，但业务逻辑已完成)
        return 1;
    }

    /**
     * 切换点赞/收藏状态
     * 优化：只清除当前展品详情缓存，不清除大屏缓存（避免高并发时缓存失效）
     * 大屏数据实时性通过：①缓存时合并 Redis 缓冲 ②定时任务同步后清缓存 保证
     */
    @Override
    @Transactional
    public int toggleAction(Long itemId, Integer type) {
        Long userId = SecurityUtils.getUserId();

        HeritageUserAction query = new HeritageUserAction();
        query.setUserId(userId);
        query.setItemId(itemId);
        query.setActionType(type);

        int count = actionMapper.selectCountByAction(query);
        int result;

        if (count == 0) {
            // 还没点过 -> 插入记录
            actionMapper.insertAction(query);
            result = heritageItemMapper.updateItemCount(itemId, type, 1, 1);
        } else {
            // 已经点过 -> 删除记录
            actionMapper.deleteAction(query);
            result = heritageItemMapper.updateItemCount(itemId, type, 0, 1);
        }

        // 只清除单个展品详情缓存，不清除大屏缓存（防止高并发时缓存雪崩）
        redisCache.deleteObject(CACHE_KEY_ITEM_DETAIL_PREFIX + itemId);

        return result;
    }

    /**
     * 查询单个展品详情 (Cache Aside + 动静分离)
     */
    @Override
    public HeritageItem selectHeritageItemByItemId(Long itemId) {
        String cacheKey = CACHE_KEY_ITEM_DETAIL_PREFIX + itemId;

        // 1. 尝试从 Redis 获取静态信息 (基本信息、描述、图片等)
        HeritageItem item = redisCache.getCacheObject(cacheKey);

        // 2. 缓存未命中，查数据库并写入缓存
        if (item == null) {
            item = heritageItemMapper.selectHeritageItemByItemId(itemId);
            if (item != null) {
                // 写入缓存，有效期 24 小时 (根据业务调整)
                redisCache.setCacheObject(cacheKey, item, 24, TimeUnit.HOURS);
            }
        }

        if (item != null) {
            // 3. 【合并浏览量】 真实浏览量 = 数据库存量 + Redis缓冲增量
            Integer bufferViews = redisCache.getCacheMapValue(CACHE_KEY_VIEW_BUFFER, String.valueOf(itemId));
            if (bufferViews != null) {
                item.setViewCount(item.getViewCount() + bufferViews);
            }

            // 4. 【动态状态】 获取当前用户的点赞/收藏状态 (这部分千人千面，不能缓存)
            Long userId = SecurityUtils.getUserId();
            if (userId != null) {
                item.setIsLiked(actionMapper.checkStatus(userId, itemId, 2) > 0);
                item.setIsCollected(actionMapper.checkStatus(userId, itemId, 3) > 0);
            }
        }

        return item;
    }

    /**
     * 获取首页驾驶舱统计数据 (双重检查锁防止缓存击穿)
     */
    @Override
    public IndexStatsVo getDashboardData() {
        // 1. 第一次检查缓存
        IndexStatsVo cachedStats = redisCache.getCacheObject(CACHE_KEY_DASHBOARD);
        if (cachedStats != null) {
            return cachedStats;
        }

        // 2. 加锁，防止高并发下多个线程同时查库 (缓存击穿保护)
        synchronized (this) {
            // 3. 第二次检查缓存 (可能在等待锁的过程中，别的线程已经把数据放进去了)
            cachedStats = redisCache.getCacheObject(CACHE_KEY_DASHBOARD);
            if (cachedStats != null) {
                return cachedStats;
            }

            // 4. 确实没有缓存，执行数据库查询 (这是最重的操作)
            IndexStatsVo stats = new IndexStatsVo();

            // --- 基础数据（聚合查询，避免全表 select + JVM size）---
            Long totalItems = heritageItemMapper.countValidHeritageItems();
            stats.setTotalItems(totalItems != null ? totalItems : 0L);
            Long views = heritageItemMapper.sumViewCount();
            // 合并 Redis 里的所有缓冲浏览量，保证大屏数据实时
            Map<String, Integer> bufferMap = redisCache.getCacheMap(CACHE_KEY_VIEW_BUFFER);
            if (bufferMap != null && !bufferMap.isEmpty()) {
                long bufferTotal = bufferMap.values().stream().filter(Objects::nonNull).mapToLong(Integer::longValue).sum();
                views = (views != null ? views : 0) + bufferTotal;
            }
            stats.setTotalViews(views);

            Long interactions = heritageItemMapper.sumTotalInteractions();
            stats.setTotalInteractions(interactions != null ? interactions : 0);

            // --- 图表数据 ---
            stats.setCategoryPie(heritageItemMapper.selectCategoryStats());

            HeritageItem topItem = heritageItemMapper.selectMostPopularItem();
            if (topItem != null) {
                stats.setCenterModelUrl(topItem.getModelFile());
                stats.setCenterModelName(topItem.getItemName());
            }

            stats.setResourceComposition(heritageItemMapper.selectResourceStats());
            stats.setInteractionStats(heritageItemMapper.selectInteractionStats());
            stats.setLatestItems(heritageItemMapper.selectLatestItems());
            stats.setWordCloud(heritageItemMapper.selectItemNames());
            stats.setTop5Items(heritageItemMapper.selectTop5Trend());

            // 5. 写入缓存，设置 2 分钟过期（本地开发短周期，部署后可调长）
            redisCache.setCacheObject(CACHE_KEY_DASHBOARD, stats, 2, TimeUnit.MINUTES);

            return stats;
        }
    }

    // ================= 以下为常规 CRUD =================
    // 修改数据时，必须清除相关缓存以保证一致性

    @Override
    public List<HeritageItem> selectHeritageItemList(HeritageItem heritageItem) {
        return heritageItemMapper.selectHeritageItemList(heritageItem);
    }

    @Override
    public int insertHeritageItem(HeritageItem heritageItem) {
        heritageItem.setCreateBy(SecurityUtils.getUsername());
        heritageItem.setCreateTime(DateUtils.getNowDate());
        heritageItem.setUpdateTime(DateUtils.getNowDate());

        int result = heritageItemMapper.insertHeritageItem(heritageItem);
        if (result > 0) {
            // 新增数据影响大屏统计，清除大屏缓存
            redisCache.deleteObject(CACHE_KEY_DASHBOARD);
        }
        return result;
    }

    @Override
    public int updateHeritageItem(HeritageItem heritageItem) {
        heritageItem.setUpdateTime(DateUtils.getNowDate());
        int result = heritageItemMapper.updateHeritageItem(heritageItem);
        if (result > 0) {
            // 修改了数据，清除该展品的详情缓存
            redisCache.deleteObject(CACHE_KEY_ITEM_DETAIL_PREFIX + heritageItem.getItemId());
            // 清除大屏缓存 (防止改了分类统计不准)
            redisCache.deleteObject(CACHE_KEY_DASHBOARD);
        }
        return result;
    }

    @Override
    public int deleteHeritageItemByItemIds(Long[] itemIds) {
        int result = heritageItemMapper.deleteHeritageItemByItemIds(itemIds);
        if (result > 0) {
            // 批量清除详情缓存
            List<String> keys = new ArrayList<>();
            for (Long id : itemIds) {
                keys.add(CACHE_KEY_ITEM_DETAIL_PREFIX + id);
            }
            redisCache.deleteObject(keys);

            // 清除大屏缓存
            redisCache.deleteObject(CACHE_KEY_DASHBOARD);
        }
        return result;
    }

    @Override
    public int deleteHeritageItemByItemId(Long itemId) {
        int result = heritageItemMapper.deleteHeritageItemByItemId(itemId);
        if (result > 0) {
            redisCache.deleteObject(CACHE_KEY_ITEM_DETAIL_PREFIX + itemId);
            redisCache.deleteObject(CACHE_KEY_DASHBOARD);
        }
        return result;
    }

    @Override
    public List<HeritageItem> selectUserCollectionList() {
        Long userId = SecurityUtils.getUserId();
        return heritageItemMapper.selectUserCollectionList(userId);
    }

    @Override
    public List<HeritageItem> selectUserPublishList(HeritageItem heritageItem) {
        return heritageItemMapper.selectMyHeritageItemList(heritageItem);
    }
}