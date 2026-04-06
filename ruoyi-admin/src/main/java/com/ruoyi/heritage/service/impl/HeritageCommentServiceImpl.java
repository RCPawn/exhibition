package com.ruoyi.heritage.service.impl;

import com.ruoyi.common.core.redis.RedisCache;
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
    private HeritageItemMapper heritageItemMapper;

    @Autowired
    private RedisCache redisCache;

    private static final String CACHE_KEY_DASHBOARD = "heritage:stats:dashboard";

    @Override
    public List<HeritageComment> selectHeritageCommentList(HeritageComment heritageComment) {
        return heritageCommentMapper.selectHeritageCommentList(heritageComment);
    }

    /**
     * 新增评论
     */
    @Override
    @Transactional
    public int insertHeritageComment(HeritageComment heritageComment) {
        heritageComment.setUserId(SecurityUtils.getUserId());

        int rows = heritageCommentMapper.insertHeritageComment(heritageComment);

        if (rows > 0) {
            heritageItemMapper.incrementCommentCount(heritageComment.getItemId());
            // 清除大屏缓存，保证评论统计实时展示
            redisCache.deleteObject(CACHE_KEY_DASHBOARD);
        }
        return rows;
    }
}