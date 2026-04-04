package com.ruoyi.heritage.domain;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 图集管理对象 heritage_gallery
 *
 * @author ruoyi
 * @date 2026-02-01
 */
public class HeritageGallery extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long galleryId;

    /**
     * 图集标题
     */
    @Excel(name = "图集标题")
    private String title;

    /**
     * 封面图
     */
    @Excel(name = "封面图")
    private String coverUrl;

    /**
     * 导语
     */
    private String description;

    /**
     * 状态
     */
    @Excel(name = "状态")
    private String status;

    /**
     * $column.columnComment
     */
    private String delFlag;

    /**
     * 图集从表信息
     */
    private List<HeritageGalleryImage> heritageGalleryImageList;

    public void setGalleryId(Long galleryId) {
        this.galleryId = galleryId;
    }

    public Long getGalleryId() {
        return galleryId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public List<HeritageGalleryImage> getHeritageGalleryImageList() {
        return heritageGalleryImageList;
    }

    public void setHeritageGalleryImageList(List<HeritageGalleryImage> heritageGalleryImageList) {
        this.heritageGalleryImageList = heritageGalleryImageList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("galleryId", getGalleryId())
                .append("title", getTitle())
                .append("coverUrl", getCoverUrl())
                .append("description", getDescription())
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("heritageGalleryImageList", getHeritageGalleryImageList())
                .toString();
    }
}
