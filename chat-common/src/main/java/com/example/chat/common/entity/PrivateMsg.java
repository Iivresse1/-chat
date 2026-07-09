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
@TableName("private_msg")
public class PrivateMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("from_user_id")
    private Integer fromUserId;

    @TableField("to_user_id")
    private Integer toUserId;

    @TableField("msg_type")
    private String msgType;

    @TableField("msg_status")
    private String msgStatus;

    @TableField("msg_is_recalled")
    private Byte msgIsRecalled;

    @TableField("msg_send_time")
    private LocalDateTime msgSendTime;

    @TableField("file_url")
    private String fileUrl;

    @TableField("file_name")
    private String fileName;

    @TableField("file_size")
    private Long fileSize;

    @TableField("content")
    private String content;
}
