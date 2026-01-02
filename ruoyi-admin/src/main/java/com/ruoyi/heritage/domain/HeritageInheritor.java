package com.ruoyi.heritage.domain;

import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.List;

/**
 * 非遗传承人对象 heritage_inheritor
 *
 * @author ruoyi
 * @date 2026-01-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeritageInheritor extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 传承人ID
     */
    @Getter
    @Setter
    private Long inheritorId;

    /**
     * 姓名
     */
    @Getter
    @Setter
    @Excel(name = "姓名")
    private String name;

    /**
     * 个人照片
     */
    @Getter
    @Excel(name = "个人照片")
    private String avatar;

    /**
     * 级别（1国家级 2省级 3市级 4县级）
     */
    @Excel(name = "级别", readConverterExp = "1=国家级,2=省级,3=市级,4=县级")
    private String level;

    /**
     * 所属技艺分类ID（关联 heritage_category）
     */
    @Excel(name = "所属技艺分类ID", readConverterExp = "关=联,h=eritage_category")
    private Long categoryId;

    /**
     * 个人简介
     */
    @Excel(name = "个人简介")
    private String introduction;

    /**
     * 状态
     */
    @Excel(name = "状态")
    private String status;

    /**
     * 删除标志
     */
    private String delFlag;

    // 增加属性存放作品ID列表
    private List<Long> itemIds;

    // 增加属性存放分类名称（用于列表显示）
    private String categoryName;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("inheritorId", getInheritorId())
                .append("name", getName())
                .append("avatar", getAvatar())
                .append("level", getLevel())
                .append("categoryId", getCategoryId())
                .append("introduction", getIntroduction())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
