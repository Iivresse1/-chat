package com.example.chat.common.service.impl;

import org.mindrot.jbcrypt.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.chat.common.entity.User;
import com.example.chat.common.mapper.UserMapper;
import com.example.chat.common.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chat.common.service.IVerifyCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private IVerifyCodeService verifyCodeService;

    @Override
    public User register(String email, String nickname, String password, String checkcode) {
        if (!verifyCodeService.verify(email, checkcode)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getEmail, email);
        if (this.getOne(qw) != null) throw new RuntimeException("该邮箱已注册");

        User user = new User();
        user.setEmail(email);
        user.setNickName(nickname);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setRegisterTime(LocalDateTime.now());
        user.setStatus((byte) 0);
        user.setOnlineStatus("online");
        this.save(user);
        return user;
    }

    @Override
    public String login(String email, String password) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getEmail, email);
        User user = this.getOne(qw);
        if (user == null) throw new RuntimeException("该用户不存在");
        if (!BCrypt.checkpw(password, user.getPassword())) throw new RuntimeException("用户名或密码错误");
        if (!user.getStatus().equals((byte) 0)) throw new RuntimeException("该用户状态异常，暂时无法登录");
        StpUtil.login(user.getId());
        return StpUtil.getTokenValue();
    }

    @Override
    public List<User> searchUser(String keyword) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.like(User::getNickName, keyword).or().like(User::getEmail, keyword).or().like(User::getPhone, keyword);
        List<User> list = this.list(qw);
        list.forEach(u -> u.setPassword(null));
        return list;
    }

    @Override
    public User getProfile(Integer userId) {
        User user = this.getById(userId);
        if (user != null) user.setPassword(null);
        return user;
    }

    @Override
    public void updateProfile(Integer userId, String nickName, String sign, Byte gender, String phone, String birthday) {
        UpdateWrapper<User> uw = new UpdateWrapper<>();
        uw.eq("id", userId)
          .set("nick_name", nickName)
          .set("sign", sign)
          .set("gender", gender)
          .set("phone", phone)
          .set("birthday", birthday);
        this.update(uw);
    }

    @Override
    public void updateAvatar(Integer userId, String avatarUrl) {
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        this.updateById(user);
    }

    @Override
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) throw new RuntimeException("原密码错误");
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        this.updateById(user);
    }

    @Override
    public void updateStatus(Integer userId, String onlineStatus) {
        User user = new User();
        user.setId(userId);
        user.setOnlineStatus(onlineStatus);
        this.updateById(user);
    }

    @Override
    public String uploadAvatar(Integer userId, byte[] fileBytes, String originalFilename) {
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String dateDir = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM"));
        java.nio.file.Path dir = java.nio.file.Paths.get("./uploads", dateDir);
        try {
            java.nio.file.Files.createDirectories(dir);
            String filename = java.util.UUID.randomUUID().toString().replace("-", "") + ext;
            java.nio.file.Path target = dir.resolve(filename);
            java.nio.file.Files.write(target, fileBytes);
            String url = "/chat/file/view/" + dateDir + "/" + filename;
            updateAvatar(userId, url);
            return url;
        } catch (java.io.IOException e) {
            throw new RuntimeException("头像上传失败: " + e.getMessage());
        }
    }

    @Override
    public String getSettings(Integer userId) {
        return "{}";
    }

    @Override
    public void updateSettings(Integer userId, String settingsJson) {
        // TODO: 设置功能待实现
    }
}
