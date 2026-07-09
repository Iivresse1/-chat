package com.example.chat.web.dto;

import lombok.Data;

@Data
public class SendPrivateMsgDto {
    private Integer toUserId;
    private String msgType;     // text / image / audio / video / file
    private String content;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
}
