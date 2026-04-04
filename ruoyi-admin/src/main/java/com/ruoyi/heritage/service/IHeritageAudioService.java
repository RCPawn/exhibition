package com.ruoyi.heritage.service;

import java.util.List;
import com.ruoyi.heritage.domain.HeritageAudio;

/**
 * 音频档案Service接口
 * 
 * @author ruoyi
 * @date 2026-01-31
 */
public interface IHeritageAudioService 
{
    /**
     * 查询音频档案
     * 
     * @param audioId 音频档案主键
     * @return 音频档案
     */
    public HeritageAudio selectHeritageAudioByAudioId(Long audioId);

    /**
     * 查询音频档案列表
     * 
     * @param heritageAudio 音频档案
     * @return 音频档案集合
     */
    public List<HeritageAudio> selectHeritageAudioList(HeritageAudio heritageAudio);

    /**
     * 新增音频档案
     * 
     * @param heritageAudio 音频档案
     * @return 结果
     */
    public int insertHeritageAudio(HeritageAudio heritageAudio);

    /**
     * 修改音频档案
     * 
     * @param heritageAudio 音频档案
     * @return 结果
     */
    public int updateHeritageAudio(HeritageAudio heritageAudio);

    /**
     * 批量删除音频档案
     * 
     * @param audioIds 需要删除的音频档案主键集合
     * @return 结果
     */
    public int deleteHeritageAudioByAudioIds(Long[] audioIds);

    /**
     * 删除音频档案信息
     * 
     * @param audioId 音频档案主键
     * @return 结果
     */
    public int deleteHeritageAudioByAudioId(Long audioId);
}
