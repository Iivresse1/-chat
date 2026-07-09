package com.example.chat.web.controller;

import com.example.chat.common.Result;
import com.example.chat.common.service.IGroupMsgService;
import com.example.chat.web.dto.SendGroupMsgDto;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/group-msg")
public class GroupMsgController {

    @Resource
    private IGroupMsgService groupMsgService;

    @PostMapping("/send")
    public Result send(@RequestBody SendGroupMsgDto dto) {
        return Result.success(groupMsgService.sendMsg(
            dto.getGroupId(), dto.getMsgType(), dto.getContent(),
            dto.getFileUrl(), dto.getFileName(), dto.getFileSize()
        ));
    }

    @GetMapping("/history")
    public Result history(@RequestParam Integer groupId,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size) {
        return Result.success(groupMsgService.history(groupId, page, size));
    }

    @PostMapping("/recall")
    public Result recall(@RequestBody Map<String, Object> params) {
        Integer msgId = Integer.valueOf(params.get("msgId").toString());
        groupMsgService.recallMsg(msgId);
        return Result.success();
    }
}
