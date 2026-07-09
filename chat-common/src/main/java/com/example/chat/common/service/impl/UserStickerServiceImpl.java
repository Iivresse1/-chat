package com.example.chat.common.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.chat.common.entity.UserSticker;
import com.example.chat.common.mapper.UserStickerMapper;
import com.example.chat.common.service.IUserStickerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserStickerServiceImpl extends ServiceImpl<UserStickerMapper, UserSticker>
        implements IUserStickerService {

    @Override
    public void addSticker(String stickerUrl, String stickerName, Byte type) {
        int loginId = StpUtil.getLoginIdAsInt();
        LambdaQueryWrapper<UserSticker> qw = new LambdaQueryWrapper<>();
        qw.eq(UserSticker::getUserId, loginId).eq(UserSticker::getStickerUrl, stickerUrl);
        if (this.getOne(qw) != null) throw new RuntimeException("该表情已收藏");

        UserSticker s = new UserSticker(); s.setUserId(loginId);
        s.setStickerUrl(stickerUrl); s.setStickerName(stickerName);
        s.setType(type); s.setStickerCount(0); s.setCreateTime(LocalDateTime.now());
        this.save(s);
    }

    @Override
    public void deleteSticker(Integer stickerId) {
        int loginId = StpUtil.getLoginIdAsInt();
        UserSticker s = this.getById(stickerId);
        if (s == null) throw new RuntimeException("表情不存在");
        if (!s.getUserId().equals(loginId)) throw new RuntimeException("只能删除自己的表情");
        this.removeById(stickerId);
    }

    @Override
    public List<UserSticker> myStickers(Byte type) {
        int loginId = StpUtil.getLoginIdAsInt();
        LambdaQueryWrapper<UserSticker> qw = new LambdaQueryWrapper<>();
        qw.eq(UserSticker::getUserId, loginId);
        if (type != null) qw.eq(UserSticker::getType, type);
        qw.orderByDesc(UserSticker::getCreateTime);
        return this.list(qw);
    }
}
