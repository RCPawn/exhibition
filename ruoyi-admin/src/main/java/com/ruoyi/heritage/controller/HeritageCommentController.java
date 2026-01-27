package com.ruoyi.heritage.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.heritage.domain.HeritageComment;
import com.ruoyi.heritage.service.IHeritageCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/heritage/comment")
public class HeritageCommentController extends BaseController {
    @Autowired
    private IHeritageCommentService heritageCommentService;

    /**
     * 查询评论列表
     */
    @GetMapping("/list")
    public TableDataInfo list(HeritageComment heritageComment) {
        startPage(); // 分页
        List<HeritageComment> list = heritageCommentService.selectHeritageCommentList(heritageComment);
        return getDataTable(list);
    }

    /**
     * 新增评论
     */
    //@PreAuthorize("@ss.hasPermi('heritage:comment:add')") // 权限控制，或者去掉变成公开接口
    @Log(title = "非遗评论", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HeritageComment heritageComment) {
        if (heritageComment.getItemId() == null) {
            return error("展品ID不能为空");
        }
        if (StringUtils.isEmpty(heritageComment.getContent())) {
            return error("评论内容不能为空");
        }
        return toAjax(heritageCommentService.insertHeritageComment(heritageComment));
    }
}