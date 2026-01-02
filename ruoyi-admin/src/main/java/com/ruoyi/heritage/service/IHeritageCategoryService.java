package com.ruoyi.heritage.service;

import java.util.List;
import com.ruoyi.heritage.domain.HeritageCategory;

/**
 * 非遗分类Service接口
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
public interface IHeritageCategoryService 
{
    /**
     * 查询非遗分类
     * 
     * @param categoryId 非遗分类主键
     * @return 非遗分类
     */
    public HeritageCategory selectHeritageCategoryByCategoryId(Long categoryId);

    /**
     * 查询非遗分类列表
     * 
     * @param heritageCategory 非遗分类
     * @return 非遗分类集合
     */
    public List<HeritageCategory> selectHeritageCategoryList(HeritageCategory heritageCategory);

    /**
     * 新增非遗分类
     * 
     * @param heritageCategory 非遗分类
     * @return 结果
     */
    public int insertHeritageCategory(HeritageCategory heritageCategory);

    /**
     * 修改非遗分类
     * 
     * @param heritageCategory 非遗分类
     * @return 结果
     */
    public int updateHeritageCategory(HeritageCategory heritageCategory);

    /**
     * 批量删除非遗分类
     * 
     * @param categoryIds 需要删除的非遗分类主键集合
     * @return 结果
     */
    public int deleteHeritageCategoryByCategoryIds(Long[] categoryIds);

    /**
     * 删除非遗分类信息
     * 
     * @param categoryId 非遗分类主键
     * @return 结果
     */
    public int deleteHeritageCategoryByCategoryId(Long categoryId);
}
