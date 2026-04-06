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

    /* =================  1. 核心 CRUD 业务  ================= */

    /** 查询展品详情 */
    HeritageItem selectHeritageItemByItemId(Long itemId);

    /** 查询展品列表 (全量/管理员/展厅) */
    List<HeritageItem> selectHeritageItemList(HeritageItem heritageItem);

    /** 查询个人发布的展品列表 (UGC 隔离) */
    List<HeritageItem> selectMyHeritageItemList(HeritageItem heritageItem);

    /** 新增展品 */
    int insertHeritageItem(HeritageItem heritageItem);

    /** 修改展品 */
    int updateHeritageItem(HeritageItem heritageItem);

    /** 删除展品 */
    int deleteHeritageItemByItemId(Long itemId);

    /** 批量删除展品 */
    int deleteHeritageItemByItemIds(Long[] itemIds);

    /* =================  2. 用户交互与计数 (原子操作)  ================= */

    /**
     * 统一更新统计数量 (通用切换逻辑)
     * @param itemId 展品ID
     * @param type   类型: 1-浏览, 2-点赞, 3-收藏
     * @param status 动作: 1-增加, 0-减少
     * @param increment 增量值 (仅type=1时使用)
     */
    int updateItemCount(@Param("itemId") Long itemId, @Param("type") Integer type, @Param("status") Integer status, @Param("increment") Integer increment);

    /** 增加评论数 */
    int incrementCommentCount(@Param("itemId") Long itemId);

    /** 查询用户个人收藏夹 */
    List<HeritageItem> selectUserCollectionList(Long userId);

    /* =================  3. 数据驾驶舱 (Cockpit) 统计接口  ================= */

    /** 统计各一级分类分布情况 */
    List<ChartDataVo> selectCategoryStats();

    /** 统计资源构成 (3D模型 vs 图文资料) */
    List<ChartDataVo> selectResourceStats();

    /** 统计全站交互行为总量 (点赞/收藏/评论) */
    List<ChartDataVo> selectInteractionStats();

    /** 获取展品名称及浏览量 (用于热词云) */
    List<ChartDataVo> selectItemNames();

    /** 获取热门展品排行 Top 5 */
    List<ChartDataVo> selectTop5Trend();

    /** 获取最新录入的 5 条展品 */
    List<HeritageItem> selectLatestItems();

    /** 获取热度最高的一个展品 (镇馆之宝) */
    HeritageItem selectMostPopularItem();

    /** 累计浏览总热度 */
    Long sumViewCount();

    /** 累计全站互动总数 */
    Long sumTotalInteractions();
}