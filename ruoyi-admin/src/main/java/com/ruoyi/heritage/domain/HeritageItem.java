package com.ruoyi.heritage.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 非遗展品对象 heritage_item
 *
 * @author ruoyi
 * @date 2025-12-25
 */
public class HeritageItem extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 展品ID */
    private Long itemId;

    /** 展品名称 */
    @Excel(name = "展品名称")
    private String itemName;

    /** 分类ID */
    @Excel(name = "分类ID")
    private Long categoryId;

    /** 封面图片URL */
    @Excel(name = "封面图片URL")
    private String coverImage;

    /** 3D模型文件路径 */
    private String modelFile;

    /** 展品简介 */
    private String description;

    /** 历史渊源 */
    private String history;

    /** 制作工艺 */
    private String craft;

    /** 浏览次数 */
    @Excel(name = "浏览次数")
    private Integer viewCount;

    /** 点赞数 */
    @Excel(name = "点赞数")
    private Integer likeCount;

    /** 收藏数 */
    @Excel(name = "收藏数")
    private Long favoriteCount;

    /** 状态（0正常,1下架） */
    @Excel(name = "状态", readConverterExp = "0=正常,1下架")
    private Integer status;

    private String rejectReason;

    /** 删除标志 */
    private String delFlag;

    /** 显示顺序 */
    private Long sortOrder;

    // --- 以下是业务扩展字段（不对应数据库，由 Service 逻辑填充） ---

    /** 当前用户是否点赞 */
    @JsonProperty("isLiked") // 核心修复：强制 JSON 字段名为 isLiked
    private boolean isLiked = false;

    /** 当前用户是否收藏 */
    @JsonProperty("isCollected") // 核心修复：强制 JSON 字段名为 isCollected
    private boolean isCollected = false;

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setModelFile(String modelFile) {
        this.modelFile = modelFile;
    }

    public String getModelFile() {
        return modelFile;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public String getHistory() {
        return history;
    }

    public void setCraft(String craft) {
        this.craft = craft;
    }

    public String getCraft() {
        return craft;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setFavoriteCount(Long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Long getFavoriteCount() {
        return favoriteCount;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setSortOrder(Long sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getSortOrder() {
        return sortOrder;
    }

    // 重新修正后的 Getter/Setter 保持标准命名
    public boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public boolean getIsCollected() {
        return isCollected;
    }

    public void setIsCollected(boolean isCollected) {
        this.isCollected = isCollected;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("itemId", getItemId())
                .append("itemName", getItemName())
                .append("categoryId", getCategoryId())
                .append("coverImage", getCoverImage())
                .append("modelFile", getModelFile())
                .append("description", getDescription())
                .append("history", getHistory())
                .append("craft", getCraft())
                .append("viewCount", getViewCount())
                .append("likeCount", getLikeCount())
                .append("favoriteCount", getFavoriteCount()) // 已修正为 favoriteCount
                .append("status", getStatus())
                .append("delFlag", getDelFlag())
                .append("sortOrder", getSortOrder())
                .append("isLiked", getIsLiked()) // toString 补充点赞状态
                .append("isCollected", getIsCollected()) // toString 补充收藏状态
                .append("remark", getRemark())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}