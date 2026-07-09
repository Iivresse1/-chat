package com.example.chat.common.service;

import com.example.chat.common.entity.GroupMsg;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface IGroupMsgService extends IService<GroupMsg> {
    GroupMsg sendMsg(Integer groupId, Byte msgType, String content,
                     String fileUrl, String fileName, Double fileSize);
    List<GroupMsg> history(Integer groupId, int page, int size);
    void recallMsg(Integer msgId);
}
