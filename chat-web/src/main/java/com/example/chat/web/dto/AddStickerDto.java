package com.example.chat.web.dto;

import lombok.Data;

@Data
public class AddStickerDto {
    private String stickerUrl;
    private String stickerName;
    private Byte type;      // 0=喜欢 1=订阅
}
