package com.example.chat.web.dto;

import lombok.Data;

@Data
public class SendFriendRequestDto {
    private Long receiveUserId;
    private String message;
}
