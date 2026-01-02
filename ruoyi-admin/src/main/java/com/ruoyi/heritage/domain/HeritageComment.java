package com.ruoyi.heritage.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 非遗展品评论对象 heritage_comment
 */
public class HeritageComment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 非遗展品ID
     */
    private Long itemId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 删除标志
     */
    private String delFlag;

    // --- 业务扩展字段 (数据库不存在，通过 JOIN 查询注入) ---

    /**
     * 评论者昵称
     */
    private String nickName;

    /**
     * 评论者头像
     */
    private String avatar;

    /**
     * ✅ 关键修复：必须显式添加无参构造方法
     * 否则 Jackson 反序列化 JSON 时会报错
     */
    public HeritageComment() {
    }

    // --- 标准 Getter / Setter 方法 ---

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    // --- 扩展字段 Getter / Setter ---

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("commentId", getCommentId())
                .append("itemId", getItemId())
                .append("userId", getUserId())
                .append("content", getContent())
                .append("createTime", getCreateTime())
                .append("nickName", getNickName())
                .toString();
    }
}