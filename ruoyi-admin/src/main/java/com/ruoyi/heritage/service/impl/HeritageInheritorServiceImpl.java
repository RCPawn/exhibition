package com.ruoyi.heritage.service.impl;

import java.util.List;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.heritage.mapper.HeritageInheritorMapper;
import com.ruoyi.heritage.domain.HeritageInheritor;
import com.ruoyi.heritage.service.IHeritageInheritorService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 非遗传承人Service业务层处理
 *
 * @author ruoyi
 * @date 2026-01-02
 */
@Service
public class HeritageInheritorServiceImpl implements IHeritageInheritorService {
    @Autowired
    private HeritageInheritorMapper heritageInheritorMapper;

    /**
     * 查询非遗传承人
     *
     * @param inheritorId 非遗传承人主键
     * @return 非遗传承人
     */
    @Override
    public HeritageInheritor selectHeritageInheritorByInheritorId(Long inheritorId) {
        return heritageInheritorMapper.selectHeritageInheritorByInheritorId(inheritorId);
    }

    /**
     * 查询非遗传承人列表
     *
     * @param heritageInheritor 非遗传承人
     * @return 非遗传承人
     */
    @Override
    public List<HeritageInheritor> selectHeritageInheritorList(HeritageInheritor heritageInheritor) {
        return heritageInheritorMapper.selectHeritageInheritorList(heritageInheritor);
    }

    /**
     * 新增非遗传承人
     *
     * @param heritageInheritor 非遗传承人
     * @return 结果
     */
    @Override
    public int insertHeritageInheritor(HeritageInheritor heritageInheritor) {
        return heritageInheritorMapper.insertHeritageInheritor(heritageInheritor);
    }

    /**
     * 修改非遗传承人
     *
     * @param heritageInheritor 非遗传承人
     * @return 结果
     */
    @Override
    public int updateHeritageInheritor(HeritageInheritor heritageInheritor) {
        return heritageInheritorMapper.updateHeritageInheritor(heritageInheritor);
    }

    /**
     * 批量删除非遗传承人
     *
     * @param inheritorIds 需要删除的非遗传承人主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteHeritageInheritorByInheritorIds(Long[] inheritorIds) {
        return heritageInheritorMapper.deleteHeritageInheritorByInheritorIds(inheritorIds);
    }

    /**
     * 删除非遗传承人信息
     *
     * @param inheritorId 非遗传承人主键
     * @return 结果
     */
    @Override
    public int deleteHeritageInheritorByInheritorId(Long inheritorId) {
        return heritageInheritorMapper.deleteHeritageInheritorByInheritorId(inheritorId);
    }
}
