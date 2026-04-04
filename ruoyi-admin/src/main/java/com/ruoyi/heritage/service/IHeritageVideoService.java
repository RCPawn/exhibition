package com.ruoyi.heritage.service;

import com.ruoyi.heritage.domain.HeritageVideo;
import java.util.List;

public interface IHeritageVideoService {
    
    public HeritageVideo selectHeritageVideoByVideoId(Long videoId);
    
    public List<HeritageVideo> selectHeritageVideoList(HeritageVideo heritageVideo);
    
    public int insertHeritageVideo(HeritageVideo heritageVideo);
    
    public int updateHeritageVideo(HeritageVideo heritageVideo);
    
    public int deleteHeritageVideoByVideoId(Long videoId);
    
    public int deleteHeritageVideoByVideoIds(Long[] videoIds);
}
