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
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.heritage.domain.HeritageGallery;
import com.ruoyi.heritage.service.IHeritageGalleryService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 图集管理Controller
 *
 * @author ruoyi
 * @date 2026-02-01
 */
@RestController
@RequestMapping("/heritage/gallery")
public class HeritageGalleryController extends BaseController {
    @Autowired
    private IHeritageGalleryService heritageGalleryService;

    /**
     * 查询图集管理列表
     */
    @GetMapping("/list")
    public TableDataInfo list(HeritageGallery heritageGallery) {
        startPage();
        List<HeritageGallery> list = heritageGalleryService.selectHeritageGalleryList(heritageGallery);
        return getDataTable(list);
    }

    /**
     * 导出图集管理列表
     */
    @PreAuthorize("@ss.hasPermi('heritage:gallery:export')")
    @Log(title = "图集管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HeritageGallery heritageGallery) {
        List<HeritageGallery> list = heritageGalleryService.selectHeritageGalleryList(heritageGallery);
        ExcelUtil<HeritageGallery> util = new ExcelUtil<HeritageGallery>(HeritageGallery.class);
        util.exportExcel(response, list, "图集管理数据");
    }

    /**
     * 获取图集管理详细信息
     */
    @GetMapping(value = "/{galleryId}")
    public AjaxResult getInfo(@PathVariable("galleryId") Long galleryId) {
        return success(heritageGalleryService.selectHeritageGalleryByGalleryId(galleryId));
    }

    /**
     * 新增图集管理
     */
    @PreAuthorize("@ss.hasPermi('heritage:gallery:add')")
    @Log(title = "图集管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HeritageGallery heritageGallery) {
        return toAjax(heritageGalleryService.insertHeritageGallery(heritageGallery));
    }

    /**
     * 修改图集管理
     */
    @PreAuthorize("@ss.hasPermi('heritage:gallery:edit')")
    @Log(title = "图集管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HeritageGallery heritageGallery) {
        return toAjax(heritageGalleryService.updateHeritageGallery(heritageGallery));
    }

    /**
     * 删除图集管理
     */
    @PreAuthorize("@ss.hasPermi('heritage:gallery:remove')")
    @Log(title = "图集管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{galleryIds}")
    public AjaxResult remove(@PathVariable Long[] galleryIds) {
        return toAjax(heritageGalleryService.deleteHeritageGalleryByGalleryIds(galleryIds));
    }
}
