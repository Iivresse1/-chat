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
@TableName("friendship")
public class Friendship implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("friend_id")
    private Long friendId;

    @TableField("friend_remark")
    private String friendRemark;

    @TableField("category")
    private String category;

    /**
     * 对某人展示的专属状态
     */
    @TableField("status_for_one_friend")
    private String statusForOneFriend;

    /**
     * 对朋友展示的当前状态
     */
    @TableField("status_to_friends")
    private String statusToFriends;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String friendNickName;
}
