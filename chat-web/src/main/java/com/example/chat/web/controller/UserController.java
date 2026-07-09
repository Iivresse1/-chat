package com.example.chat.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.chat.common.Result;
import com.example.chat.common.service.IUserService;
import com.example.chat.web.dto.LoginDto;
import com.example.chat.web.dto.RegisterDto;
import jakarta.annotation.Resource;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(400, bindingResult.getAllErrors().get(0).getDefaultMessage(), null);
        }
        userService.register(dto.getEmail(), dto.getNickname(), dto.getPassword(), dto.getCheckcode());
        return Result.success();
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginDto dto) {
        String token = userService.login(dto.getEmail(), dto.getPassword());
        return Result.success(token);
    }

    @GetMapping("/search")
    public Result search(@RequestParam String keyword) {
        return Result.success(userService.searchUser(keyword));
    }

    @GetMapping("/profile")
    public Result profile() {
        int userId = StpUtil.getLoginIdAsInt();
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result updateProfile(@RequestBody Map<String, Object> params) {
        int userId = StpUtil.getLoginIdAsInt();
        userService.updateProfile(
            userId,
            (String) params.get("nickName"),
            (String) params.get("sign"),
            params.get("gender") != null ? Byte.valueOf(params.get("gender").toString()) : null,
            (String) params.get("phone"),
            (String) params.get("birthday")
        );
        return Result.success();
    }

    @PutMapping("/avatar")
    public Result updateAvatar(@RequestBody Map<String, String> params) {
        int userId = StpUtil.getLoginIdAsInt();
        userService.updateAvatar(userId, params.get("avatarUrl"));
        return Result.success();
    }

    @PostMapping("/avatar/upload")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(400, "文件为空", null);
        int userId = StpUtil.getLoginIdAsInt();
        try {
            String url = userService.uploadAvatar(userId, file.getBytes(), file.getOriginalFilename());
            return Result.success(url);
        } catch (IOException e) {
            return Result.error(500, "头像上传失败: " + e.getMessage(), null);
        }
    }

    @PutMapping("/password")
    public Result changePassword(@RequestBody Map<String, String> params) {
        int userId = StpUtil.getLoginIdAsInt();
        userService.changePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return Result.success();
    }

    @PutMapping("/status")
    public Result updateStatus(@RequestBody Map<String, String> params) {
        int userId = StpUtil.getLoginIdAsInt();
        userService.updateStatus(userId, params.get("status"));
        return Result.success();
    }

    @GetMapping("/settings")
    public Result getSettings() {
        int userId = StpUtil.getLoginIdAsInt();
        return Result.success(userService.getSettings(userId));
    }

    @PutMapping("/settings")
    public Result updateSettings(@RequestBody Map<String, Object> params) {
        int userId = StpUtil.getLoginIdAsInt();
        userService.updateSettings(userId, params.toString());
        return Result.success();
    }
}
