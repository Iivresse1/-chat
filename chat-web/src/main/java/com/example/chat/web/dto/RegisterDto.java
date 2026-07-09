package com.example.chat.web.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


    /*
    注册数据类
    */
@Data
public class RegisterDto {

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
    private String email;
        @NotBlank(message = "昵称不能为空")
        @Size(min = 6, max = 16, message = "昵称长度6-16个字符")
    private String nickname;
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 16, message = "密码长度6-16位")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "密码必须包含字母和数字")
    private String password;

    private String checkcode;
}
