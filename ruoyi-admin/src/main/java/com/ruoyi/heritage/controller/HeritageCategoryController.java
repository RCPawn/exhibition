package com.ruoyi.heritage.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.heritage.domain.HeritageCategory;
import com.ruoyi.heritage.service.IHeritageCategoryService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 非遗分类Controller
 *
 * @author ruoyi
 * @date 2025-12-25
 */
@RestController
@RequestMapping("/heritage/category")
public class HeritageCategoryController extends BaseController {
    @Autowired
    private IHeritageCategoryService heritageCategoryService;


    /**
     * 获取分类树形下拉列表
     */
    @Anonymous
    @GetMapping("/treeselect")
    public AjaxResult treeselect(HeritageCategory category) {
        List<HeritageCategory> categories = heritageCategoryService.selectHeritageCategoryList(category);
        return AjaxResult.success(heritageCategoryService.buildCategoryTree(categories));
    }

    /**
     * 查询非遗分类列表
     */
//    @PreAuthorize("@ss.hasPermi('heritage:category:list')")
    @Anonymous
    @GetMapping("/list")
    public TableDataInfo list(HeritageCategory heritageCategory) {
        startPage();
        List<HeritageCategory> list = heritageCategoryService.selectHeritageCategoryList(heritageCategory);
        return getDataTable(list);
    }

    /**
     * 导出非遗分类列表
     */
    @PreAuthorize("@ss.hasPermi('heritage:category:export')")
    @Log(title = "非遗分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HeritageCategory heritageCategory) {
        List<HeritageCategory> list = heritageCategoryService.selectHeritageCategoryList(heritageCategory);
        ExcelUtil<HeritageCategory> util = new ExcelUtil<HeritageCategory>(HeritageCategory.class);
        util.exportExcel(response, list, "非遗分类数据");
    }

    /**
     * 获取非遗分类详细信息
     */
    // @PreAuthorize("@ss.hasPermi('heritage:category:query')")
    @Anonymous
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId) {
        return success(heritageCategoryService.selectHeritageCategoryByCategoryId(categoryId));
    }

    /**
     * 新增非遗分类
     */
    @PreAuthorize("@ss.hasPermi('heritage:category:add')")
    @Log(title = "非遗分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HeritageCategory heritageCategory) {
        return toAjax(heritageCategoryService.insertHeritageCategory(heritageCategory));
    }

    /**
     * 修改非遗分类
     */
    @PreAuthorize("@ss.hasPermi('heritage:category:edit')")
    @Log(title = "非遗分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HeritageCategory heritageCategory) {
        return toAjax(heritageCategoryService.updateHeritageCategory(heritageCategory));
    }

    /**
     * 删除非遗分类
     */
    @PreAuthorize("@ss.hasPermi('heritage:category:remove')")
    @Log(title = "非遗分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds) {
        return toAjax(heritageCategoryService.deleteHeritageCategoryByCategoryIds(categoryIds));
    }
}
