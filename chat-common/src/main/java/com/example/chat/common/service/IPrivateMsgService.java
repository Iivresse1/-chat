package com.example.chat.common.service;

import com.example.chat.common.entity.PrivateMsg;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface IPrivateMsgService extends IService<PrivateMsg> {
    PrivateMsg sendMsg(Integer toUserId, String msgType, String content,
                       String fileUrl, String fileName, Long fileSize);
    List<PrivateMsg> history(Integer withUserId, int page, int size);
    void recallMsg(Long msgId);
    void markRead(Integer fromUserId);
    int unreadCount(Integer fromUserId);
}
