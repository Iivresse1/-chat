package com.example.chat.web.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupInfoVo {
    private Integer id;
    private String groupName;
    private String announcement;
    private Long ownerId;
    private Integer maxMembers;
    private Integer memberCount;
    private LocalDateTime createTime;
    private Byte status;
}
