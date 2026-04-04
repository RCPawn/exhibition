package com.ruoyi.heritage.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.heritage.mapper.HeritageAudioMapper;
import com.ruoyi.heritage.domain.HeritageAudio;
import com.ruoyi.heritage.service.IHeritageAudioService;

/**
 * 音频档案Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-01-31
 */
@Service
public class HeritageAudioServiceImpl implements IHeritageAudioService 
{
    @Autowired
    private HeritageAudioMapper heritageAudioMapper;

    /**
     * 查询音频档案
     * 
     * @param audioId 音频档案主键
     * @return 音频档案
     */
    @Override
    public HeritageAudio selectHeritageAudioByAudioId(Long audioId)
    {
        return heritageAudioMapper.selectHeritageAudioByAudioId(audioId);
    }

    /**
     * 查询音频档案列表
     * 
     * @param heritageAudio 音频档案
     * @return 音频档案
     */
    @Override
    public List<HeritageAudio> selectHeritageAudioList(HeritageAudio heritageAudio)
    {
        return heritageAudioMapper.selectHeritageAudioList(heritageAudio);
    }

    /**
     * 新增音频档案
     * 
     * @param heritageAudio 音频档案
     * @return 结果
     */
    @Override
    public int insertHeritageAudio(HeritageAudio heritageAudio)
    {
        heritageAudio.setCreateTime(DateUtils.getNowDate());
        return heritageAudioMapper.insertHeritageAudio(heritageAudio);
    }

    /**
     * 修改音频档案
     * 
     * @param heritageAudio 音频档案
     * @return 结果
     */
    @Override
    public int updateHeritageAudio(HeritageAudio heritageAudio)
    {
        heritageAudio.setUpdateTime(DateUtils.getNowDate());
        return heritageAudioMapper.updateHeritageAudio(heritageAudio);
    }

    /**
     * 批量删除音频档案
     * 
     * @param audioIds 需要删除的音频档案主键
     * @return 结果
     */
    @Override
    public int deleteHeritageAudioByAudioIds(Long[] audioIds)
    {
        return heritageAudioMapper.deleteHeritageAudioByAudioIds(audioIds);
    }

    /**
     * 删除音频档案信息
     * 
     * @param audioId 音频档案主键
     * @return 结果
     */
    @Override
    public int deleteHeritageAudioByAudioId(Long audioId)
    {
        return heritageAudioMapper.deleteHeritageAudioByAudioId(audioId);
    }
}
