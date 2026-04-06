package com.ruoyi.heritage.task;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.heritage.domain.HeritageItem;
import com.ruoyi.heritage.mapper.HeritageItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 非遗业务定时任务
 */
@Component("heritageTask")
public class HeritageTask {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private HeritageItemMapper heritageItemMapper;

    private static final String CACHE_KEY_VIEW_BUFFER = "heritage:view_buffer";
    private static final String CACHE_KEY_DASHBOARD = "heritage:stats:dashboard";
    private static final String CACHE_KEY_ITEM_DETAIL_PREFIX = "heritage:item:detail:";

    /**
     * 同步浏览量：Redis -> MySQL
     * 本地开发每 10 秒执行一次，生产环境建议 1-5 分钟
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncViewCount() {
        // 1. 获取 Redis 中所有的浏览量数据
        Map<String, Integer> viewMap = redisCache.getCacheMap(CACHE_KEY_VIEW_BUFFER);

        if (viewMap == null || viewMap.isEmpty()) {
            return;
        }

        boolean hasSynced = false;

        // 2. 遍历并更新到数据库
        for (Map.Entry<String, Integer> entry : viewMap.entrySet()) {
            String itemIdStr = entry.getKey();
            Integer viewAdd = entry.getValue();

            if (viewAdd == null || viewAdd == 0) {
                continue;
            }

            try {
                Long itemId = Long.parseLong(itemIdStr);
                // 更新数据库： type=1(浏览量), status=1(增加), increment=viewAdd
                heritageItemMapper.updateItemCount(itemId, 1, 1, viewAdd);
                
                // 3. 数据库更新成功后，扣除 Redis 中的相应数量
                redisCache.incrementCacheMapValue(CACHE_KEY_VIEW_BUFFER, itemIdStr, -viewAdd);
                hasSynced = true;
                
                // 4. 【关键修复】同步后必须清除该展品的详情缓存
                // 否则下次访问会读取缓存中的旧 viewCount，导致数据倒退
                redisCache.deleteObject(CACHE_KEY_ITEM_DETAIL_PREFIX + itemId);
                
            } catch (Exception e) {
                // 捕获异常，防止单条数据错误导致整个任务回滚
                System.err.println("同步非遗展品浏览量失败: " + itemIdStr);
            }
        }

        // 5. 同步完成后清除大屏缓存，保证数据实时展示
        if (hasSynced) {
            redisCache.deleteObject(CACHE_KEY_DASHBOARD);
        }
    }
}