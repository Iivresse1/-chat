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
@TableName("group_msg")
public class GroupMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 群聊id
     */
    @TableField("group_id")
    private Integer groupId;

    /**
     * 发送消息的用户id
     */
    @TableField("from_user_id")
    private Integer fromUserId;

    /**
     * 1文本 2图片 3语音 4视频 5文件
     */
    @TableField("msg_type")
    private Byte msgType;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    @TableField("file_url")
    private String fileUrl;

    @TableField("file_name")
    private String fileName;

    @TableField("file_size")
    private Double fileSize;

    /**
     * 消息撤回状态
     */
    @TableField("msg_is_recalled")
    private Byte msgIsRecalled;

    /**
     * @的用户
     */
    @TableField("msg_memtion_user_id")
    private String msgMemtionUserId;

    /**
     * 消息撤回时间
     */
    @TableField("msg_recall_time")
    private LocalDateTime msgRecallTime;

    /**
     * 消息发送时间
     */
    @TableField("msg_send_time")
    private LocalDateTime msgSendTime;
}
