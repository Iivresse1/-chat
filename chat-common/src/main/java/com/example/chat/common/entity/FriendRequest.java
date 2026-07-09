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
@TableName("friend_request")
public class FriendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申请id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 好友申请发送方
     */
    @TableField("send_user")
    private Long sendUser;

    /**
     * 好友申请接收方
     */
    @TableField("receive_user")
    private Long receiveUser;

    /**
     * 验证消息
     */
    @TableField("message")
    private String message;

    /**
     * 好友申请状态
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;

    @TableField(exist = false)
    private String senderNickName;
}
