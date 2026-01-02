package com.ruoyi.heritage.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
public class HeritageCategoryServiceImpl implements IHeritageCategoryService {
    @Autowired
    private HeritageCategoryMapper heritageCategoryMapper;

    /**
     * 构建前端所需要的树结构
     */
    @Override
    public List<HeritageCategory> buildCategoryTree(List<HeritageCategory> categories) {
        List<HeritageCategory> returnList = new ArrayList<HeritageCategory>();
        List<Long> tempList = categories.stream().map(HeritageCategory::getCategoryId).collect(Collectors.toList());

        for (HeritageCategory category : categories) {
            // 如果是顶级节点（parentId = 0），则开始递归寻找子节点
            if (!tempList.contains(category.getParentId())) {
                recursionFn(categories, category);
                returnList.add(category);
            }
        }
        if (returnList.isEmpty()) {
            returnList = categories;
        }
        return returnList;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<HeritageCategory> list, HeritageCategory t) {
        // 得到子节点列表
        List<HeritageCategory> childList = getChildList(list, t);
        t.setChildren(childList);
        for (HeritageCategory tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<HeritageCategory> getChildList(List<HeritageCategory> list, HeritageCategory t) {
        List<HeritageCategory> tlist = new ArrayList<HeritageCategory>();
        for (HeritageCategory n : list) {
            if (n.getParentId().longValue() == t.getCategoryId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<HeritageCategory> list, HeritageCategory t) {
        return getChildList(list, t).size() > 0;
    }

    /**
     * 查询非遗分类
     *
     * @param categoryId 非遗分类主键
     * @return 非遗分类
     */
    @Override
    public HeritageCategory selectHeritageCategoryByCategoryId(Long categoryId) {
        return heritageCategoryMapper.selectHeritageCategoryByCategoryId(categoryId);
    }

    /**
     * 查询非遗分类列表
     *
     * @param heritageCategory 非遗分类
     * @return 非遗分类
     */
    @Override
    public List<HeritageCategory> selectHeritageCategoryList(HeritageCategory heritageCategory) {
        return heritageCategoryMapper.selectHeritageCategoryList(heritageCategory);
    }

    /**
     * 新增非遗分类
     *
     * @param heritageCategory 非遗分类
     * @return 结果
     */
    @Override
    public int insertHeritageCategory(HeritageCategory heritageCategory) {
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
    public int updateHeritageCategory(HeritageCategory heritageCategory) {
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
    public int deleteHeritageCategoryByCategoryIds(Long[] categoryIds) {
        return heritageCategoryMapper.deleteHeritageCategoryByCategoryIds(categoryIds);
    }

    /**
     * 删除非遗分类信息
     *
     * @param categoryId 非遗分类主键
     * @return 结果
     */
    @Override
    public int deleteHeritageCategoryByCategoryId(Long categoryId) {
        return heritageCategoryMapper.deleteHeritageCategoryByCategoryId(categoryId);
    }
}
