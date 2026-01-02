package com.ruoyi.heritage.mapper;

import com.ruoyi.heritage.domain.HeritageComment;

import java.util.List;

public interface HeritageCommentMapper
{
    /**
     * 查询评论列表（包含用户信息）
     */
    public List<HeritageComment> selectHeritageCommentList(HeritageComment heritageComment);

    /**
     * 新增评论
     */
    public int insertHeritageComment(HeritageComment heritageComment);
}