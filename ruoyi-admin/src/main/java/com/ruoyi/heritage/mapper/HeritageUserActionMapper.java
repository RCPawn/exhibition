package com.ruoyi.heritage.mapper;

import com.ruoyi.heritage.domain.HeritageUserAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户点赞收藏记录Mapper接口
 */
@Mapper
public interface HeritageUserActionMapper {

    /**
     * 查询记录是否存在
     */
    public int selectCountByAction(HeritageUserAction query);

    /**
     * 插入点赞收藏记录
     */
    public int insertAction(HeritageUserAction query);

    /**
     * 删除点赞收藏记录
     */
    public int deleteAction(HeritageUserAction query);

    /**
     * 检查用户状态 (1-点赞, 2-收藏)
     */
    public int checkStatus(@Param("userId") Long userId, @Param("itemId") Long itemId, @Param("actionType") int actionType);
}