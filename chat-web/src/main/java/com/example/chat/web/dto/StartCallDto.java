package com.example.chat.web.dto;

import lombok.Data;

@Data
public class StartCallDto {
    private Long calleeId;
    private Byte callType;      // 1=私聊语音 2=群语音
    private Integer groupId;    // 群通话时传
}
