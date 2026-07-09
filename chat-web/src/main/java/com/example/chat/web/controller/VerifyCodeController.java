package com.example.chat.web.controller;

import com.example.chat.common.Result;
import com.example.chat.common.service.IVerifyCodeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/verify-code")
public class VerifyCodeController {

    @Resource
    private IVerifyCodeService verifyCodeService;

    @PostMapping("/send")
    public Result send(@RequestBody Map<String, Object> params) {
        try {
            String email = params.get("email").toString();
            verifyCodeService.sendCode(email);
            return Result.success();
        } catch (Exception e) {
            return Result.error(400, e.getMessage(), null);
        }
    }
}
