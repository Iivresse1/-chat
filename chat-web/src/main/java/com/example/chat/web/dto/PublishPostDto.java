package com.example.chat.web.dto;

import lombok.Data;

@Data
public class PublishPostDto {
    private String content;
    private String images;      // 逗号分隔的图片URL
    private String location;
    private Byte status;        // 0=公开 1=仅好友 2=私密
}
