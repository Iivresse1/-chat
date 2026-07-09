package com.example.chat.common.service;

import com.example.chat.common.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface IUserService extends IService<User> {
    User register(String email, String nickname, String password, String checkcode);
    String login(String email, String password);
    List<User> searchUser(String keyword);
    User getProfile(Integer userId);
    void updateProfile(Integer userId, String nickName, String sign, Byte gender, String phone, String birthday);
    void updateAvatar(Integer userId, String avatarUrl);
    void changePassword(Integer userId, String oldPassword, String newPassword);
    void updateStatus(Integer userId, String onlineStatus);
    String uploadAvatar(Integer userId, byte[] fileBytes, String originalFilename);
    String getSettings(Integer userId);
    void updateSettings(Integer userId, String settingsJson);
}
