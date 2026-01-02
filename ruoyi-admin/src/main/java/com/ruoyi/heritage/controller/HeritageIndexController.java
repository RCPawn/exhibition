package com.ruoyi.heritage.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.heritage.domain.HeritageItem;
import com.ruoyi.heritage.domain.IndexStatsVo;
import com.ruoyi.heritage.mapper.HeritageItemMapper;
import com.ruoyi.heritage.service.IHeritageItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/heritage/stats")
public class HeritageIndexController extends BaseController {

    @Autowired
    private IHeritageItemService heritageItemService;

    @GetMapping("/dashboard")
    public AjaxResult getDashboardData() {
        IndexStatsVo stats = heritageItemService.getDashboardData();
        return success(stats);
    }
}