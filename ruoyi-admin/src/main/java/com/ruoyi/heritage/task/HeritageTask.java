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

    /**
     * 同步浏览量：Redis -> MySQL
     * 建议每 5-10 分钟执行一次
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncViewCount() {
        // 1. 获取 Redis 中所有的浏览量数据
        // Map<String(itemId), Integer(viewCount)>
        Map<String, Integer> viewMap = redisCache.getCacheMap(CACHE_KEY_VIEW_BUFFER);

        if (viewMap == null || viewMap.isEmpty()) {
            return;
        }

        // 2. 遍历并更新到数据库
        for (Map.Entry<String, Integer> entry : viewMap.entrySet()) {
            String itemIdStr = entry.getKey();
            Integer viewAdd = entry.getValue();

            if (viewAdd == null || viewAdd == 0) {
                continue;
            }

            try {
                Long itemId = Long.parseLong(itemIdStr);
                // 更新数据库： update heritage_item set view_count = view_count + #{viewAdd} where item_id = #{itemId}
                // 注意：这里复用你现有的 updateItemCount 方法，或者单独写一个
                // 参数含义：itemId, type=1(浏览量), increment=viewAdd
                heritageItemMapper.updateItemCount(itemId, 1, viewAdd);
                
                // 3. 数据库更新成功后，扣除 Redis 中的相应数量
                // 为什么不直接 delete？因为在同步的这几毫秒内，可能又有用户点击了，直接删会导致这几毫秒的数据丢失。
                // 使用 increment 负数来抵消，是最安全的做法。
                redisCache.incrementCacheMapValue(CACHE_KEY_VIEW_BUFFER, itemIdStr, -viewAdd);
                
            } catch (Exception e) {
                // 捕获异常，防止单条数据错误导致整个任务回滚
                System.err.println("同步非遗展品浏览量失败: " + itemIdStr);
            }
        }
    }
}