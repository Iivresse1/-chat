package com.example.chat.web.controller;

import com.example.chat.common.Result;
import com.example.chat.common.service.IGroupInfoService;
import com.example.chat.web.dto.CreateGroupDto;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/group")
public class GroupController {

    @Resource
    private IGroupInfoService groupInfoService;

    @PostMapping("/create")
    public Result create(@RequestBody CreateGroupDto dto) {
        return Result.success(groupInfoService.createGroup(dto.getGroupName()));
    }

    @PostMapping("/join")
    public Result join(@RequestBody Map<String, Object> params) {
        Integer groupId = Integer.valueOf(params.get("groupId").toString());
        groupInfoService.joinGroup(groupId);
        return Result.success();
    }

    @PostMapping("/leave")
    public Result leave(@RequestBody Map<String, Object> params) {
        Integer groupId = Integer.valueOf(params.get("groupId").toString());
        groupInfoService.leaveGroup(groupId);
        return Result.success();
    }

    @PostMapping("/dismiss")
    public Result dismiss(@RequestBody Map<String, Object> params) {
        Integer groupId = Integer.valueOf(params.get("groupId").toString());
        groupInfoService.dismissGroup(groupId);
        return Result.success();
    }

    @PostMapping("/rename")
    public Result rename(@RequestBody Map<String, Object> params) {
        Integer groupId = Integer.valueOf(params.get("groupId").toString());
        String groupName = params.get("groupName").toString();
        groupInfoService.updateGroupName(groupId, groupName);
        return Result.success();
    }

    @GetMapping("/members")
    public Result members(@RequestParam Integer groupId) {
        return Result.success(groupInfoService.memberList(groupId));
    }
}
