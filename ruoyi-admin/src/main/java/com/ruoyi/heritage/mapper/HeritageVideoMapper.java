package com.ruoyi.heritage.mapper;

import com.ruoyi.heritage.domain.HeritageVideo;
import java.util.List;

public interface HeritageVideoMapper {
    
    public HeritageVideo selectHeritageVideoByVideoId(Long videoId);
    
    public List<HeritageVideo> selectHeritageVideoList(HeritageVideo heritageVideo);
    
    public int insertHeritageVideo(HeritageVideo heritageVideo);
    
    public int updateHeritageVideo(HeritageVideo heritageVideo);
    
    public int deleteHeritageVideoByVideoId(Long videoId);
    
    public int deleteHeritageVideoByVideoIds(Long[] videoIds);
}
