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
@TableName("user_sticker")
public class UserSticker implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Integer userId;

    @TableField("sticker_url")
    private String stickerUrl;

    @TableField("sticker_name")
    private String stickerName;

    /**
     * 0个人喜欢的表情1聊天软件订阅的表情包
     */
    @TableField("type")
    private Byte type;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("sticker_count")
    private Integer stickerCount;
}
