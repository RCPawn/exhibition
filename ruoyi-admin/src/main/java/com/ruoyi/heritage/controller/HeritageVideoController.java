package com.ruoyi.heritage.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.heritage.domain.HeritageVideo;
import com.ruoyi.heritage.service.IHeritageVideoService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/heritage/video")
public class HeritageVideoController extends BaseController {
    
    @Autowired
    private IHeritageVideoService heritageVideoService;

    @Anonymous
    @GetMapping("/listArchive")
    public TableDataInfo listArchive(HeritageVideo video) {
        startPage();
        video.setStatus("0");
        List<HeritageVideo> list = heritageVideoService.selectHeritageVideoList(video);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('heritage:video:list')")
    @GetMapping("/list")
    public TableDataInfo list(HeritageVideo heritageVideo) {
        startPage();
        List<HeritageVideo> list = heritageVideoService.selectHeritageVideoList(heritageVideo);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('heritage:video:export')")
    @Log(title = "视频档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HeritageVideo heritageVideo) {
        List<HeritageVideo> list = heritageVideoService.selectHeritageVideoList(heritageVideo);
        ExcelUtil<HeritageVideo> util = new ExcelUtil<>(HeritageVideo.class);
        util.exportExcel(response, list, "视频档案数据");
    }

    @PreAuthorize("@ss.hasPermi('heritage:video:query')")
    @GetMapping(value = "/{videoId}")
    public AjaxResult getInfo(@PathVariable("videoId") Long videoId) {
        return success(heritageVideoService.selectHeritageVideoByVideoId(videoId));
    }

    @PreAuthorize("@ss.hasPermi('heritage:video:add')")
    @Log(title = "视频档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HeritageVideo video) {
        video.setCreateBy(SecurityUtils.getUsername());
        return toAjax(heritageVideoService.insertHeritageVideo(video));
    }

    @PreAuthorize("@ss.hasPermi('heritage:video:edit')")
    @Log(title = "视频档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HeritageVideo heritageVideo) {
        return toAjax(heritageVideoService.updateHeritageVideo(heritageVideo));
    }

    @PreAuthorize("@ss.hasPermi('heritage:video:remove')")
    @Log(title = "视频档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{videoIds}")
    public AjaxResult remove(@PathVariable Long[] videoIds) {
        return toAjax(heritageVideoService.deleteHeritageVideoByVideoIds(videoIds));
    }
}
