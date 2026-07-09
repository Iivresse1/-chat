package com.example.chat.common.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.chat.common.entity.FriendRequest;
import com.example.chat.common.entity.Friendship;
import com.example.chat.common.mapper.FriendRequestMapper;
import com.example.chat.common.service.IFriendRequestService;
import com.example.chat.common.entity.User;
import com.example.chat.common.service.IFriendshipService;
import com.example.chat.common.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendRequestServiceImpl extends ServiceImpl<FriendRequestMapper, FriendRequest>
        implements IFriendRequestService {

    @Resource
    private IFriendshipService friendshipService;

    @Resource
    private IUserService userService;

    @Override
    public void sendRequest(Long receiveUserId, String message) {
        long loginId = StpUtil.getLoginIdAsLong();
        if (loginId == receiveUserId) throw new RuntimeException("不能添加自己为好友");

        LambdaQueryWrapper<Friendship> fw = new LambdaQueryWrapper<>();
        fw.eq(Friendship::getUserId, loginId).eq(Friendship::getFriendId, receiveUserId);
        if (friendshipService.getOne(fw) != null) throw new RuntimeException("已经是好友");

        LambdaQueryWrapper<FriendRequest> qw = new LambdaQueryWrapper<>();
        qw.eq(FriendRequest::getSendUser, loginId).eq(FriendRequest::getReceiveUser, receiveUserId)
          .eq(FriendRequest::getStatus, "pending");
        if (this.getOne(qw) != null) throw new RuntimeException("已发送过申请");

        FriendRequest req = new FriendRequest();
        req.setSendUser(loginId); req.setReceiveUser(receiveUserId);
        req.setMessage(message); req.setStatus("pending"); req.setCreateTime(LocalDateTime.now());
        this.save(req);
    }

    @Override
    public List<FriendRequest> pendingList() {
        long loginId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<FriendRequest> qw = new LambdaQueryWrapper<>();
        qw.eq(FriendRequest::getReceiveUser, loginId).eq(FriendRequest::getStatus, "pending");
        List<FriendRequest> list = this.list(qw);
        if (!list.isEmpty()) {
            Set<Integer> senderIds = list.stream().map(r -> r.getSendUser().intValue()).collect(Collectors.toSet());
            Map<Integer, String> nickMap = userService.listByIds(senderIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u.getNickName() != null ? u.getNickName() : ""));
            list.forEach(r -> r.setSenderNickName(nickMap.getOrDefault(r.getSendUser().intValue(), "")));
        }
        return list;
    }

    @Override
    public void handleRequest(Long requestId, String status) {
        FriendRequest req = this.getById(requestId);
        if (req == null) throw new RuntimeException("申请不存在");
        long loginId = StpUtil.getLoginIdAsLong();
        if (!req.getReceiveUser().equals(loginId)) throw new RuntimeException("无权操作");

        req.setStatus(status); req.setHandleTime(LocalDateTime.now()); this.updateById(req);

        if ("accepted".equals(status)) {
            // Fetch nicknames for friendRemark
            User sendUser = userService.getById(req.getSendUser().intValue());
            User receiveUser = userService.getById(req.getReceiveUser().intValue());
            String sendNick = sendUser != null && sendUser.getNickName() != null ? sendUser.getNickName() : "";
            String recvNick = receiveUser != null && receiveUser.getNickName() != null ? receiveUser.getNickName() : "";

            Friendship f1 = new Friendship(); f1.setUserId(req.getSendUser()); f1.setFriendId(req.getReceiveUser()); f1.setFriendRemark(recvNick); f1.setCreateTime(LocalDateTime.now()); friendshipService.save(f1);
            Friendship f2 = new Friendship(); f2.setUserId(req.getReceiveUser()); f2.setFriendId(req.getSendUser()); f2.setFriendRemark(sendNick); f2.setCreateTime(LocalDateTime.now()); friendshipService.save(f2);
        }
    }
}
