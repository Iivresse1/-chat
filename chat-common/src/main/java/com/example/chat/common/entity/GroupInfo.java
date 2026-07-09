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
@TableName("group_info")
public class GroupInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 群名
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 群公告
     */
    @TableField("announcement")
    private String announcement;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("max_members")
    private Integer maxMembers;

    @TableField("member_count")
    private Integer memberCount;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("status")
    private Byte status;
}
