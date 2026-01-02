package com.ruoyi.heritage.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class HeritageUserAction extends BaseEntity {
    private Long actionId;
    private Long userId;
    private Long itemId;
    private Integer actionType; // 1-点赞, 2-收藏

    // Getter & Setter ...
    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Integer getActionType() { return actionType; }
    public void setActionType(Integer actionType) { this.actionType = actionType; }
}