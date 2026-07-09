package com.example.chat.web.controller;

import com.example.chat.common.Result;
import com.example.chat.common.service.IFriendRequestService;
import com.example.chat.web.dto.SendFriendRequestDto;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/friend-request")
public class FriendRequestController {

    @Resource
    private IFriendRequestService friendRequestService;

    @PostMapping("/send")
    public Result send(@RequestBody SendFriendRequestDto dto) {
        friendRequestService.sendRequest(dto.getReceiveUserId(), dto.getMessage());
        return Result.success();
    }

    @GetMapping("/pending")
    public Result pending() {
        return Result.success(friendRequestService.pendingList());
    }

    @PostMapping("/handle")
    public Result handle(@RequestBody Map<String, Object> params) {
        Long requestId = Long.valueOf(params.get("requestId").toString());
        String status = params.get("status").toString();
        friendRequestService.handleRequest(requestId, status);
        return Result.success();
    }
}
