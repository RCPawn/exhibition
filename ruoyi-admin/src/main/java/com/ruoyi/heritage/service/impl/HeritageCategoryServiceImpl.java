package com.ruoyi.heritage.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.heritage.mapper.HeritageCategoryMapper;
import com.ruoyi.heritage.domain.HeritageCategory;
import com.ruoyi.heritage.service.IHeritageCategoryService;

/**
 * 非遗分类Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
@Service
public class HeritageCategoryServiceImpl implements IHeritageCategoryService 
{
    @Autowired
    private HeritageCategoryMapper heritageCategoryMapper;

    /**
     * 查询非遗分类
     * 
     * @param categoryId 非遗分类主键
     * @return 非遗分类
     */
    @Override
    public HeritageCategory selectHeritageCategoryByCategoryId(Long categoryId)
    {
        return heritageCategoryMapper.selectHeritageCategoryByCategoryId(categoryId);
    }

    /**
     * 查询非遗分类列表
     * 
     * @param heritageCategory 非遗分类
     * @return 非遗分类
     */
    @Override
    public List<HeritageCategory> selectHeritageCategoryList(HeritageCategory heritageCategory)
    {
        return heritageCategoryMapper.selectHeritageCategoryList(heritageCategory);
    }

    /**
     * 新增非遗分类
     * 
     * @param heritageCategory 非遗分类
     * @return 结果
     */
    @Override
    public int insertHeritageCategory(HeritageCategory heritageCategory)
    {
        heritageCategory.setCreateTime(DateUtils.getNowDate());
        return heritageCategoryMapper.insertHeritageCategory(heritageCategory);
    }

    /**
     * 修改非遗分类
     * 
     * @param heritageCategory 非遗分类
     * @return 结果
     */
    @Override
    public int updateHeritageCategory(HeritageCategory heritageCategory)
    {
        heritageCategory.setUpdateTime(DateUtils.getNowDate());
        return heritageCategoryMapper.updateHeritageCategory(heritageCategory);
    }

    /**
     * 批量删除非遗分类
     * 
     * @param categoryIds 需要删除的非遗分类主键
     * @return 结果
     */
    @Override
    public int deleteHeritageCategoryByCategoryIds(Long[] categoryIds)
    {
        return heritageCategoryMapper.deleteHeritageCategoryByCategoryIds(categoryIds);
    }

    /**
     * 删除非遗分类信息
     * 
     * @param categoryId 非遗分类主键
     * @return 结果
     */
    @Override
    public int deleteHeritageCategoryByCategoryId(Long categoryId)
    {
        return heritageCategoryMapper.deleteHeritageCategoryByCategoryId(categoryId);
    }
}
