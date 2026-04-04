package com.ruoyi.heritage.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = false)
public class HeritageVideo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 视频 ID */
    private Long videoId;

    /** 视频标题 */
    @Excel(name = "视频标题")
    private String title;

    /** 视频文件路径 */
    private String videoUrl;

    /** 封面图片 URL */
    private String coverImage;

    /** 关联展品 ID */
    @Excel(name = "关联展品 ID")
    private Long itemId;

    /** 视频时长 (秒) */
    private Integer duration;

    /** 视频简介/解说词 */
    private String description;

    /** 状态 (0 正常 1 待审核 2 下架) */
    @Excel(name = "状态 (0 正常 1 待审核 2 下架)")
    private String status;

    /** 删除标志 */
    private String delFlag;

    // 业务扩展字段（不对应数据库）
    private String modelFile; // 展品模型路径
    private String itemName;   // 展品名字

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("videoId", getVideoId())
                .append("title", getTitle())
                .append("videoUrl", getVideoUrl())
                .append("coverImage", getCoverImage())
                .append("itemId", getItemId())
                .append("duration", getDuration())
                .append("description", getDescription())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
