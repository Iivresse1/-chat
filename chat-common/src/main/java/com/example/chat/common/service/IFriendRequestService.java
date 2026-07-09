package com.example.chat.common.service;

import com.example.chat.common.entity.FriendRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface IFriendRequestService extends IService<FriendRequest> {
    void sendRequest(Long receiveUserId, String message);
    List<FriendRequest> pendingList();
    void handleRequest(Long requestId, String status);
}
