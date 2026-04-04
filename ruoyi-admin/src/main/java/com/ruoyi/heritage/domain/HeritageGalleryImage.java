package com.ruoyi.heritage.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 图集从表对象 heritage_gallery_image
 * 
 * @author ruoyi
 * @date 2026-02-01
 */
public class HeritageGalleryImage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long imageId;

    /** 关联主表ID */
    @Excel(name = "关联主表ID")
    private Long galleryId;

    /** 图片路径 */
    @Excel(name = "图片路径")
    private String imageUrl;

    /** 图片说明 */
    @Excel(name = "图片说明")
    private String caption;

    /** 排序 */
    private Long sortOrder;

    public void setImageId(Long imageId) 
    {
        this.imageId = imageId;
    }

    public Long getImageId() 
    {
        return imageId;
    }
    public void setGalleryId(Long galleryId) 
    {
        this.galleryId = galleryId;
    }

    public Long getGalleryId() 
    {
        return galleryId;
    }
    public void setImageUrl(String imageUrl) 
    {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() 
    {
        return imageUrl;
    }
    public void setCaption(String caption) 
    {
        this.caption = caption;
    }

    public String getCaption() 
    {
        return caption;
    }
    public void setSortOrder(Long sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Long getSortOrder() 
    {
        return sortOrder;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("imageId", getImageId())
            .append("galleryId", getGalleryId())
            .append("imageUrl", getImageUrl())
            .append("caption", getCaption())
            .append("sortOrder", getSortOrder())
            .toString();
    }
}
