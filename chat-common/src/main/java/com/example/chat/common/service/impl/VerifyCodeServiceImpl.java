package com.example.chat.common.service.impl;

import com.example.chat.common.service.IVerifyCodeService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerifyCodeServiceImpl implements IVerifyCodeService {

    @Value("${spring.mail.username}")
    private String from;

    @Resource
    private JavaMailSender mailSender;

    /** email -> {code, expireTime} */
    private final ConcurrentHashMap<String, CodeEntry> store = new ConcurrentHashMap<>();

    private static class CodeEntry {
        final String code;
        final long expireAt;
        CodeEntry(String code, long expireAt) { this.code = code; this.expireAt = expireAt; }
    }

    @Override
    public void sendCode(String email) {
        // sweep expired entries
        store.entrySet().removeIf(e -> e.getValue().expireAt < System.currentTimeMillis());

        String code = String.format("%06d", new Random().nextInt(999999));
        store.put(email, new CodeEntry(code, System.currentTimeMillis() + 60_000));

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(email);
        msg.setSubject("αChat 验证码");
        msg.setText("您的验证码是：" + code + "，60秒内有效。");
        mailSender.send(msg);
    }

    @Override
    public boolean verify(String email, String code) {
        CodeEntry entry = store.get(email);
        if (entry == null) return false;
        if (System.currentTimeMillis() > entry.expireAt) {
            store.remove(email);
            return false;
        }
        if (entry.code.equals(code)) {
            store.remove(email); // one-time use
            return true;
        }
        return false;
    }
}
