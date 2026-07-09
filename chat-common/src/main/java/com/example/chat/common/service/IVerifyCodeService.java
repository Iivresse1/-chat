package com.example.chat.common.service;

public interface IVerifyCodeService {
    void sendCode(String email);
    boolean verify(String email, String code);
}
