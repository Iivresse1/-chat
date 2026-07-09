package com.example.chat.web.controller;

import com.example.chat.common.Result;
import com.example.chat.common.service.IVoiceCallRecordService;
import com.example.chat.web.dto.StartCallDto;
import com.example.chat.web.vo.CallStatsVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/call")
public class CallController {

    @Resource
    private IVoiceCallRecordService callService;

    @PostMapping("/start")
    public Result start(@RequestBody StartCallDto dto) {
        return Result.success(callService.startCall(dto.getCalleeId(), dto.getCallType(), dto.getGroupId()));
    }

    @PostMapping("/answer")
    public Result answer(@RequestBody Map<String, Object> params) {
        Long callId = Long.valueOf(params.get("callId").toString());
        callService.answerCall(callId);
        return Result.success();
    }

    @PostMapping("/end")
    public Result end(@RequestBody Map<String, Object> params) {
        Long callId = Long.valueOf(params.get("callId").toString());
        callService.endCall(callId);
        return Result.success();
    }

    @PostMapping("/reject")
    public Result reject(@RequestBody Map<String, Object> params) {
        Long callId = Long.valueOf(params.get("callId").toString());
        callService.rejectCall(callId);
        return Result.success();
    }

    @GetMapping("/history")
    public Result history(@RequestParam Long withUserId) {
        return Result.success(callService.callHistory(withUserId));
    }

    @GetMapping("/stats")
    public Result stats(@RequestParam Long withUserId) {
        CallStatsVo vo = new CallStatsVo();//新建CallStatsVo对象，并从后端调回数据返回前端
        vo.setCallCount(callService.callCount(withUserId));
        vo.setTotalDuration(callService.totalDuration(withUserId));
        vo.setRecords(callService.callHistory(withUserId));
        return Result.success(vo);
    }
}
