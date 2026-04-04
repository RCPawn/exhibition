package com.ruoyi.heritage.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 音频档案对象 heritage_audio
 *
 * @author ruoyi
 * @date 2026-01-31
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = false)
public class HeritageAudio extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 音频ID
     */
    private Long audioId;

    /**
     * 音频标题
     */
    @Excel(name = "音频标题")
    private String title;

    /**
     * 音频文件路径
     */
    private String audioUrl;

    /**
     * 关联展品ID (用于调取3D模型)
     */
    @Excel(name = "关联展品ID (用于调取3D模型)")
    private Long itemId;

    /**
     * 预生成的波形数据 (JSON字符串)
     */
    private String waveformData;

    /**
     * 内容解析/唱词
     */
    private String description;

    /**
     * 状态 (0正常 1待审核 2下架)
     */
    @Excel(name = "状态 (0正常 1待审核 2下架)")
    private String status;

    /**
     * 删除标志
     */
    private String delFlag;

    // 必须手动加，用于给前端返回关联资产
    private String modelFile; // 展品模型路径
    private String coverImage; // 展品图片路径
    private String itemName;   // 展品名字

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("audioId", getAudioId())
                .append("title", getTitle())
                .append("audioUrl", getAudioUrl())
                .append("itemId", getItemId())
                .append("waveformData", getWaveformData())
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
