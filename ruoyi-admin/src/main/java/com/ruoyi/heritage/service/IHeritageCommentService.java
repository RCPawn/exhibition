package com.ruoyi.heritage.service;

import java.util.List;
import com.ruoyi.heritage.domain.HeritageComment;

/**
 * 非遗展品评论Service接口
 * 
 * @author ruoyi
 */
public interface IHeritageCommentService 
{
    /**
     * 查询非遗展品评论列表
     * 
     * @param heritageComment 非遗展品评论
     * @return 非遗展品评论集合
     */
    public List<HeritageComment> selectHeritageCommentList(HeritageComment heritageComment);

    /**
     * 新增非遗展品评论
     * 
     * @param heritageComment 非遗展品评论
     * @return 结果
     */
    public int insertHeritageComment(HeritageComment heritageComment);
}