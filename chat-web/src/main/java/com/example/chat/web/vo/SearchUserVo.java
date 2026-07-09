package com.example.chat.web.vo;

import lombok.Data;

@Data
public class SearchUserVo {
    private Integer id;
    private String nickName;
    private String email;
    private String phone;
    private String sign;
    private String avatar;
    private Byte gender;
}
