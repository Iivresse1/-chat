package com.example.chat.web.dto;

import lombok.Data;

@Data
public class SendGroupMsgDto {
    private Integer groupId;
    private Byte msgType;       // 1=文本 2=图片 3=语音 4=视频 5=文件
    private String content;
    private String fileUrl;
    private String fileName;
    private Double fileSize;
}
