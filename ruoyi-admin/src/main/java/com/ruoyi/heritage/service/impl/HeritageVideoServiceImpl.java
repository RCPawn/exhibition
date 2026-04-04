package com.ruoyi.heritage.service.impl;

import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.heritage.mapper.HeritageVideoMapper;
import com.ruoyi.heritage.domain.HeritageVideo;
import com.ruoyi.heritage.service.IHeritageVideoService;

import java.util.List;

@Service
public class HeritageVideoServiceImpl implements IHeritageVideoService {
    
    @Autowired
    private HeritageVideoMapper heritageVideoMapper;

    @Override
    public HeritageVideo selectHeritageVideoByVideoId(Long videoId) {
        return heritageVideoMapper.selectHeritageVideoByVideoId(videoId);
    }

    @Override
    public List<HeritageVideo> selectHeritageVideoList(HeritageVideo heritageVideo) {
        return heritageVideoMapper.selectHeritageVideoList(heritageVideo);
    }

    @Override
    public int insertHeritageVideo(HeritageVideo heritageVideo) {
        heritageVideo.setCreateTime(DateUtils.getNowDate());
        return heritageVideoMapper.insertHeritageVideo(heritageVideo);
    }

    @Override
    public int updateHeritageVideo(HeritageVideo heritageVideo) {
        heritageVideo.setUpdateTime(DateUtils.getNowDate());
        return heritageVideoMapper.updateHeritageVideo(heritageVideo);
    }

    @Override
    public int deleteHeritageVideoByVideoId(Long videoId) {
        return heritageVideoMapper.deleteHeritageVideoByVideoId(videoId);
    }

    @Override
    public int deleteHeritageVideoByVideoIds(Long[] videoIds) {
        for (Long videoId : videoIds) {
            deleteHeritageVideoByVideoId(videoId);
        }
        return 0;
    }
}
