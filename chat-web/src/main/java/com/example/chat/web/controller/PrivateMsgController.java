package com.example.chat.web.controller;

import com.example.chat.common.Result;
import com.example.chat.common.service.IPrivateMsgService;
import com.example.chat.web.dto.SendPrivateMsgDto;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/private-msg")
public class PrivateMsgController {

    @Resource
    private IPrivateMsgService privateMsgService;

    @PostMapping("/send")
    public Result send(@RequestBody SendPrivateMsgDto dto) {
        return Result.success(privateMsgService.sendMsg(
            dto.getToUserId(), dto.getMsgType(), dto.getContent(),
            dto.getFileUrl(), dto.getFileName(), dto.getFileSize()
        ));
    }

    @GetMapping("/history")
    public Result history(@RequestParam Integer withUserId,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size) {
        return Result.success(privateMsgService.history(withUserId, page, size));
    }

    @PostMapping("/recall")
    public Result recall(@RequestBody Map<String, Object> params) {
        Long msgId = Long.valueOf(params.get("msgId").toString());
        privateMsgService.recallMsg(msgId);
        return Result.success();
    }

    @PostMapping("/mark-read")
    public Result markRead(@RequestBody Map<String, Object> params) {
        Integer fromUserId = Integer.valueOf(params.get("fromUserId").toString());
        privateMsgService.markRead(fromUserId);
        return Result.success();
    }

    @GetMapping("/unread-count")
    public Result unreadCount(@RequestParam Integer fromUserId) {
        return Result.success(privateMsgService.unreadCount(fromUserId));
    }
}
