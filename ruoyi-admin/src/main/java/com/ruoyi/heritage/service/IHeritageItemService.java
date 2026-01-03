package com.ruoyi.heritage.service;

import java.util.List;

import com.ruoyi.heritage.domain.HeritageItem;
import com.ruoyi.heritage.domain.IndexStatsVo;
import org.springframework.transaction.annotation.Transactional;

/**
 * 非遗展品Service接口
 *
 * @author ruoyi
 * @date 2025-12-25
 */
public interface IHeritageItemService {

    // 在实现类中调用 mapper
    int addViewCount(Long itemId);

    int addLikeCount(Long itemId);

    int addFavoriteCount(Long itemId);

    @Transactional
        // 开启事务
    int toggleAction(Long itemId, Integer type);

    /**
     * 查询非遗展品
     *
     * @param itemId 非遗展品主键
     * @return 非遗展品
     */
    HeritageItem selectHeritageItemByItemId(Long itemId);

    /**
     * 查询非遗展品列表
     *
     * @param heritageItem 非遗展品
     * @return 非遗展品集合
     */
    List<HeritageItem> selectHeritageItemList(HeritageItem heritageItem);

    /**
     * 新增非遗展品
     *
     * @param heritageItem 非遗展品
     * @return 结果
     */
    int insertHeritageItem(HeritageItem heritageItem);

    /**
     * 修改非遗展品
     *
     * @param heritageItem 非遗展品
     * @return 结果
     */
    int updateHeritageItem(HeritageItem heritageItem);

    /**
     * 批量删除非遗展品
     *
     * @param itemIds 需要删除的非遗展品主键集合
     * @return 结果
     */
    int deleteHeritageItemByItemIds(Long[] itemIds);

    /**
     * 删除非遗展品信息
     *
     * @param itemId 非遗展品主键
     * @return 结果
     */
    int deleteHeritageItemByItemId(Long itemId);

    /**
     * 查询当前登录用户的收藏列表
     */
    List<HeritageItem> selectUserCollectionList();

    /**
     * 获取首页驾驶舱统计数据
     * @return 统计大屏VO对象
     */
    IndexStatsVo getDashboardData();

    List<HeritageItem> selectUserPublishList(HeritageItem heritageItem);
}
