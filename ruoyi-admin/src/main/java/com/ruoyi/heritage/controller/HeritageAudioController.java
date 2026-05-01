package com.ruoyi.heritage.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.utils.SecurityUtils;
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
import com.ruoyi.heritage.domain.HeritageAudio;
import com.ruoyi.heritage.service.IHeritageAudioService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 音频档案Controller
 *
 * @author ruoyi
 * @date 2026-01-31
 */
@RestController
@RequestMapping("/heritage/audio")
public class HeritageAudioController extends BaseController {
    @Autowired
    private IHeritageAudioService heritageAudioService;

    /**
     * 1. 门户/展厅专用接口：获取已发布的音频档案
     * 路径：GET /heritage/audio/listArchive
     */
    @Anonymous
    @GetMapping("/listArchive")
    public TableDataInfo listArchive(HeritageAudio audio) {
        startPage();
        // 强制只查询状态为“已发布 (0)”的数据
        audio.setStatus("0");
        // 调用原有的 service，由于 XML 已经改成了 JOIN 查询，这里拿到的数据会包含 modelFile
        List<HeritageAudio> list = heritageAudioService.selectHeritageAudioList(audio);
        return getDataTable(list);
    }

    /**
     * 查询音频档案列表
     */
    @PreAuthorize("@ss.hasPermi('heritage:audio:list')")
    @GetMapping("/list")
    public TableDataInfo list(HeritageAudio heritageAudio) {
        startPage();
        List<HeritageAudio> list = heritageAudioService.selectHeritageAudioList(heritageAudio);
        return getDataTable(list);
    }

    /**
     * 导出音频档案列表
     */
    @PreAuthorize("@ss.hasPermi('heritage:audio:export')")
    @Log(title = "音频档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HeritageAudio heritageAudio) {
        List<HeritageAudio> list = heritageAudioService.selectHeritageAudioList(heritageAudio);
        ExcelUtil<HeritageAudio> util = new ExcelUtil<HeritageAudio>(HeritageAudio.class);
        util.exportExcel(response, list, "音频档案数据");
    }

    /**
     * 获取音频档案详细信息
     */
    @PreAuthorize("@ss.hasPermi('heritage:audio:query')")
    @GetMapping(value = "/{audioId}")
    public AjaxResult getInfo(@PathVariable("audioId") Long audioId) {
        return success(heritageAudioService.selectHeritageAudioByAudioId(audioId));
    }

    /**
     * 新增音频档案
     */
    @PreAuthorize("@ss.hasPermi('heritage:audio:add')")
    @Log(title = "音频档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HeritageAudio audio) {
        // 手动填充当前登录人为创建者
        audio.setCreateBy(SecurityUtils.getUsername());
        return toAjax(heritageAudioService.insertHeritageAudio(audio));
    }

    /**
     * 修改音频档案
     */
    @PreAuthorize("@ss.hasPermi('heritage:audio:edit')")
    @Log(title = "音频档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HeritageAudio heritageAudio) {
        return toAjax(heritageAudioService.updateHeritageAudio(heritageAudio));
    }

    /**
     * 删除音频档案
     */
    @PreAuthorize("@ss.hasPermi('heritage:audio:remove')")
    @Log(title = "音频档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{audioIds}")
    public AjaxResult remove(@PathVariable Long[] audioIds) {
        return toAjax(heritageAudioService.deleteHeritageAudioByAudioIds(audioIds));
    }
}
