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
@TableName("group_member")
public class GroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群内昵称
     */
    @TableField("group_nick_name")
    private String groupNickName;

    /**
     * 0成员，1管理，2群主
     */
    @TableField("group_role")
    private Byte groupRole;

    /**
     * 默认0正常，1禁言
     */
    @TableField("group_status")
    private Byte groupStatus;

    /**
     * 禁言解禁时间
     */
    @TableField("group_mute_expire_time")
    private LocalDateTime groupMuteExpireTime;

    /**
     * 加入群聊时间
     */
    @TableField("group_join_time")
    private LocalDateTime groupJoinTime;

    @TableField("user_id")
    private Long userId;

    @TableField("group_id")
    private Long groupId;

    /**
     * 最后一次打开群聊的时间
     */
    @TableField("last_read_time")
    private LocalDateTime lastReadTime;
}
