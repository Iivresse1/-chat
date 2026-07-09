package com.example.chat.common.service;

import com.example.chat.common.entity.UserSticker;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface IUserStickerService extends IService<UserSticker> {
    void addSticker(String stickerUrl, String stickerName, Byte type);
    void deleteSticker(Integer stickerId);
    List<UserSticker> myStickers(Byte type);
}
