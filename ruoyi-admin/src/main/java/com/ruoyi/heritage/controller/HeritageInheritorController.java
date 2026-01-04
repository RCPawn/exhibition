package com.ruoyi.heritage.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.heritage.domain.HeritageCategory;
import com.ruoyi.heritage.service.IHeritageCategoryService;
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
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.heritage.domain.HeritageInheritor;
import com.ruoyi.heritage.service.IHeritageInheritorService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 非遗传承人Controller
 *
 * @author ruoyi
 * @date 2026-01-02
 */
@RestController
@RequestMapping("/heritage/inheritor")
public class HeritageInheritorController extends BaseController {
    @Autowired
    private IHeritageInheritorService heritageInheritorService;

    @Autowired
    private IHeritageCategoryService heritageCategoryService;

    /**
     * 查询非遗传承人列表
     */
    @PreAuthorize("@ss.hasPermi('heritage:inheritor:list')")
    @GetMapping("/list")
    public TableDataInfo list(HeritageInheritor heritageInheritor) {
        startPage();
        List<HeritageInheritor> list = heritageInheritorService.selectHeritageInheritorList(heritageInheritor);
        return getDataTable(list);
    }

    /**
     * 导出非遗传承人列表
     */
    @PreAuthorize("@ss.hasPermi('heritage:inheritor:export')")
    @Log(title = "非遗传承人", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HeritageInheritor heritageInheritor) {
        List<HeritageInheritor> list = heritageInheritorService.selectHeritageInheritorList(heritageInheritor);
        ExcelUtil<HeritageInheritor> util = new ExcelUtil<HeritageInheritor>(HeritageInheritor.class);
        util.exportExcel(response, list, "非遗传承人数据");
    }

    /**
     * 获取非遗传承人详细信息
     */
    @PreAuthorize("@ss.hasPermi('heritage:inheritor:query')")
    @GetMapping(value = "/{inheritorId}")
    public AjaxResult getInfo(@PathVariable("inheritorId") Long inheritorId) {
        return success(heritageInheritorService.selectHeritageInheritorByInheritorId(inheritorId));
    }

    /**
     * 新增非遗传承人
     */
    @PreAuthorize("@ss.hasPermi('heritage:inheritor:add')")
    @Log(title = "非遗传承人", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HeritageInheritor heritageInheritor) {
        return toAjax(heritageInheritorService.insertHeritageInheritor(heritageInheritor));
    }

    /**
     * 修改非遗传承人
     */
    @PreAuthorize("@ss.hasPermi('heritage:inheritor:edit')")
    @Log(title = "非遗传承人", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HeritageInheritor heritageInheritor) {
        return toAjax(heritageInheritorService.updateHeritageInheritor(heritageInheritor));
    }

    /**
     * 删除非遗传承人
     */
    @PreAuthorize("@ss.hasPermi('heritage:inheritor:remove')")
    @Log(title = "非遗传承人", businessType = BusinessType.DELETE)
    @DeleteMapping("/{inheritorIds}")
    public AjaxResult remove(@PathVariable Long[] inheritorIds) {
        return toAjax(heritageInheritorService.deleteHeritageInheritorByInheritorIds(inheritorIds));
    }

    /**
     * 根据技艺分类获取传承人列表 (前台详情页调用)
     */
    @GetMapping("/listByCategory/{categoryId}")
    public TableDataInfo listByCategory(@PathVariable("categoryId") Long categoryId) {
        HeritageInheritor inheritor = new HeritageInheritor();
        inheritor.setCategoryId(categoryId);
        inheritor.setStatus("0"); // 只查询在世的
        List<HeritageInheritor> list = heritageInheritorService.selectHeritageInheritorList(inheritor);
        return getDataTable(list);
    }

}
