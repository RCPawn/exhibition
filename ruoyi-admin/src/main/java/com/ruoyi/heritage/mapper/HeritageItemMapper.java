package com.ruoyi.heritage.mapper;

import java.util.List;
import com.ruoyi.heritage.domain.ChartDataVo;
import com.ruoyi.heritage.domain.HeritageItem;
import org.apache.ibatis.annotations.Param;

/**
 * 非遗展品Mapper接口
 *
 * @author ruoyi
 * @date 2025-12-25
 */
public interface HeritageItemMapper {

    public int incrementViewCount(Long itemId);
    public int incrementLikeCount(Long itemId);
    public int incrementFavoriteCount(Long itemId);

    /**
     * 查询非遗展品
     *
     * @param itemId 非遗展品主键
     * @return 非遗展品
     */
    public HeritageItem selectHeritageItemByItemId(Long itemId);

    /**
     * 查询非遗展品列表
     *
     * @param heritageItem 非遗展品
     * @return 非遗展品集合
     */
    public List<HeritageItem> selectHeritageItemList(HeritageItem heritageItem);

    /**
     * 新增非遗展品
     *
     * @param heritageItem 非遗展品
     * @return 结果
     */
    public int insertHeritageItem(HeritageItem heritageItem);

    /**
     * 修改非遗展品
     *
     * @param heritageItem 非遗展品
     * @return 结果
     */
    public int updateHeritageItem(HeritageItem heritageItem);

    /**
     * 删除非遗展品
     *
     * @param itemId 非遗展品主键
     * @return 结果
     */
    public int deleteHeritageItemByItemId(Long itemId);

    /**
     * 批量删除非遗展品
     *
     * @param itemIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHeritageItemByItemIds(Long[] itemIds);

//    int updateItemCount(Long itemId, Integer type, int i);
    /**
     * 更新展品统计数量
     * @param itemId 展品ID
     * @param type   类型: 1-浏览, 2-点赞, 3-收藏
     * @param status 动作: 1-增加, 0-减少
     */
    int updateItemCount(@Param("itemId") Long itemId, @Param("type") Integer type, @Param("status") Integer status);

    public int incrementCommentCount(@Param("itemId") Long itemId);

    /**
     * 查询用户收藏列表
     */
    public List<HeritageItem> selectUserCollectionList(Long userId);


    // 数据驾驶舱相关
    /**
     * 修改这里：不再返回 Map，而是返回 ChartDataVo
     */
    List<ChartDataVo> selectCategoryStats();
    HeritageItem selectMostPopularItem();
    Long sumViewCount();
    List<ChartDataVo> selectResourceStats();
    List<ChartDataVo> selectInteractionStats();
    List<HeritageItem> selectLatestItems();
    List<ChartDataVo> selectItemNames();
    List<ChartDataVo> selectTop5Trend();
    /**
     * 统计全站互动总数
     */
    Long sumTotalInteractions();
}
