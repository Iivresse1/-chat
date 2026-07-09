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

@Getter
@Setter
@ToString
@TableName("`user`")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("nick_name")
    private String nickName;

    @TableField("sign")
    private String sign;

    @TableField("gender")
    private Byte gender;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @TableField("register_time")
    private LocalDateTime registerTime;

    @TableField("status")
    private Byte status;

    @TableField("email")
    private String email;

    @TableField("password")
    private String password;

    @TableField("phone")
    private String phone;

    @TableField("avatar")
    private String avatar;

    @TableField("birthday")
    private String birthday;

    @TableField("online_status")
    private String onlineStatus;
}
