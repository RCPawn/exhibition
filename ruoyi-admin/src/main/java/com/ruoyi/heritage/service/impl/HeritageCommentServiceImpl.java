package com.ruoyi.heritage.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.heritage.domain.HeritageComment;
import com.ruoyi.heritage.mapper.HeritageCommentMapper;
import com.ruoyi.heritage.mapper.HeritageItemMapper;
import com.ruoyi.heritage.service.IHeritageCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HeritageCommentServiceImpl implements IHeritageCommentService {
    @Autowired
    private HeritageCommentMapper heritageCommentMapper;

    @Autowired
    private HeritageItemMapper heritageItemMapper; // 注入主表 Mapper

    @Override
    public List<HeritageComment> selectHeritageCommentList(HeritageComment heritageComment) {
        return heritageCommentMapper.selectHeritageCommentList(heritageComment);
    }

    /**
     * 新增评论
     */
    @Override
    @Transactional // 开启事务
    public int insertHeritageComment(HeritageComment heritageComment) {
        // 1. 获取当前登录用户ID (若依封装工具类)
        heritageComment.setUserId(SecurityUtils.getUserId());

        // 2. 插入评论
        int rows = heritageCommentMapper.insertHeritageComment(heritageComment);

        // 3. 只有插入成功，才去增加主表的统计数据
        if (rows > 0) {
            // 这里假设你在 HeritageItemMapper 增加了一个 incrementCommentCount 方法
            // 如果没加，也可以用 select -> set -> update 的方式，但原子更新更安全
            heritageItemMapper.incrementCommentCount(heritageComment.getItemId());
        }
        return rows;
    }
}