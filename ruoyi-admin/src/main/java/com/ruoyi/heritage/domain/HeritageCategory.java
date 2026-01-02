package com.ruoyi.heritage.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 非遗分类对象 heritage_category
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeritageCategory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    private Long categoryId;

    private Long parentId; // 父ID

    /**
     * 分类名称
     */
    @Excel(name = "分类名称")
    private String categoryName;

    /**
     * 分类编码（可用于前端/接口过滤）
     */
    @Excel(name = "分类编码", readConverterExp = "可=用于前端/接口过滤")
    private String categoryCode;

    /**
     * 显示顺序（越小越靠前）
     */
    @Excel(name = "显示顺序", readConverterExp = "越=小越靠前")
    private Integer sortOrder;

    /**
     * 状态（0=正常,1=停用）
     */
    @Excel(name = "状态", readConverterExp = "0==正常,1=停用")
    private Integer status;

    /**
     * 删除标志（0=未删,2=已删）
     */
    private String delFlag;

    /**
     * 子部门
     */
    private List<HeritageCategory> children = new ArrayList<HeritageCategory>();


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                // 核心业务字段
                .append("categoryId", getCategoryId())
                .append("parentId", getParentId()) // 父分类ID
                .append("categoryName", getCategoryName())
                .append("categoryCode", getCategoryCode())
                .append("sortOrder", getSortOrder())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                // 子分类列表（层级结构关键）
                .append("children", getChildren()) // 子分类列表
                // 父类BaseEntity的字段
                .append("remark", getRemark())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
