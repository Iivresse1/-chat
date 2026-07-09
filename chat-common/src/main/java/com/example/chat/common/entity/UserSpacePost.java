package com.example.chat.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2026-06-05
 */
@Getter
@Setter
@ToString
@TableName("user_space_post")
public class UserSpacePost implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 图片
     */
    @TableField("images")
    private String images;

    /**
     * 位置
     */
    @TableField("location")
    private String location;

    /**
     * 浏览数
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 评论数
     */
    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 发布时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 0公开，1仅好友，2私密
     */
    @TableField("status")
    private Byte status;

    @TableField(exist = false)
    private String userNickName;

    @TableField(exist = false)
    private String userAvatar;
}
