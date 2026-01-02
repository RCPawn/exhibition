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
import com.ruoyi.heritage.domain.HeritageItem;
import com.ruoyi.heritage.service.IHeritageItemService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 非遗展品Controller
 *
 * @author ruoyi
 * @date 2025-12-25
 */
@RestController
@RequestMapping("/heritage/heritage_manage")
public class HeritageItemController extends BaseController {
    @Autowired
    private IHeritageItemService heritageItemService;

    /**
     * 展品交互统计 (前台调用)
     * type: 1-浏览(直接增加), 2-点赞(切换状态), 3-收藏(切换状态)
     */
    @PostMapping("/count/{type}/{itemId}")
    public AjaxResult handleHeritageAction(@PathVariable("type") Integer type, @PathVariable("itemId") Long itemId) {
        // 1. 浏览量逻辑：无需判断用户，直接自增
        if (type == 1) {
            return toAjax(heritageItemService.addViewCount(itemId));
        }

        // 2. 点赞和收藏逻辑：进入 Service 层的 toggle 事务逻辑
        // 内部会根据当前登录用户是否已有点赞记录，自动决定是 +1 还是 -1
        if (type == 2 || type == 3) {
            return toAjax(heritageItemService.toggleAction(itemId, type));
        }

        return error("非法操作类型");
    }

    /**
     * 查询非遗展品列表
     */
    @PreAuthorize("@ss.hasPermi('heritage:heritage_manage:list')")
    @GetMapping("/list")
    public TableDataInfo list(HeritageItem heritageItem) {
        startPage();
        List<HeritageItem> list = heritageItemService.selectHeritageItemList(heritageItem);
        return getDataTable(list);
    }

    /**
     * 导出非遗展品列表
     */
    @PreAuthorize("@ss.hasPermi('heritage:heritage_manage:export')")
    @Log(title = "非遗展品", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HeritageItem heritageItem) {
        List<HeritageItem> list = heritageItemService.selectHeritageItemList(heritageItem);
        ExcelUtil<HeritageItem> util = new ExcelUtil<HeritageItem>(HeritageItem.class);
        util.exportExcel(response, list, "非遗展品数据");
    }

    /**
     * 获取非遗展品详细信息
     */
    @PreAuthorize("@ss.hasPermi('heritage:heritage_manage:query')")
    @GetMapping(value = "/{itemId}")
    public AjaxResult getInfo(@PathVariable("itemId") Long itemId) {
        return success(heritageItemService.selectHeritageItemByItemId(itemId));
    }

    /**
     * 新增非遗展品
     */
    @PreAuthorize("@ss.hasPermi('heritage:heritage_manage:add')")
    @Log(title = "非遗展品", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HeritageItem heritageItem) {
        return toAjax(heritageItemService.insertHeritageItem(heritageItem));
    }

    /**
     * 修改非遗展品
     */
    @PreAuthorize("@ss.hasPermi('heritage:heritage_manage:edit')")
    @Log(title = "非遗展品", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HeritageItem heritageItem) {
        return toAjax(heritageItemService.updateHeritageItem(heritageItem));
    }

    /**
     * 删除非遗展品
     */
    @PreAuthorize("@ss.hasPermi('heritage:heritage_manage:remove')")
    @Log(title = "非遗展品", businessType = BusinessType.DELETE)
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds) {
        return toAjax(heritageItemService.deleteHeritageItemByItemIds(itemIds));
    }

    /**
     * 查询我的收藏列表
     */
    @GetMapping("/list/collection")
    public TableDataInfo listMyCollection() {
        startPage(); // 开启分页
        List<HeritageItem> list = heritageItemService.selectUserCollectionList();
        return getDataTable(list);
    }
}
