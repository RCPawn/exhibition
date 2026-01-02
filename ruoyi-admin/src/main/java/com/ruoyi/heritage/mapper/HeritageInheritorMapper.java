package com.ruoyi.heritage.mapper;

import java.util.List;
import com.ruoyi.heritage.domain.HeritageInheritor;
import org.apache.ibatis.annotations.Param;

/**
 * 非遗传承人Mapper接口
 * 
 * @author ruoyi
 * @date 2026-01-02
 */
public interface HeritageInheritorMapper 
{
    /**
     * 查询非遗传承人
     * 
     * @param inheritorId 非遗传承人主键
     * @return 非遗传承人
     */
    public HeritageInheritor selectHeritageInheritorByInheritorId(Long inheritorId);

    /**
     * 查询非遗传承人列表
     * 
     * @param heritageInheritor 非遗传承人
     * @return 非遗传承人集合
     */
    public List<HeritageInheritor> selectHeritageInheritorList(HeritageInheritor heritageInheritor);

    /**
     * 新增非遗传承人
     * 
     * @param heritageInheritor 非遗传承人
     * @return 结果
     */
    public int insertHeritageInheritor(HeritageInheritor heritageInheritor);

    /**
     * 修改非遗传承人
     * 
     * @param heritageInheritor 非遗传承人
     * @return 结果
     */
    public int updateHeritageInheritor(HeritageInheritor heritageInheritor);

    /**
     * 删除非遗传承人
     * 
     * @param inheritorId 非遗传承人主键
     * @return 结果
     */
    public int deleteHeritageInheritorByInheritorId(Long inheritorId);

    /**
     * 批量删除非遗传承人
     * 
     * @param inheritorIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteHeritageInheritorByInheritorIds(Long[] inheritorIds);

}
