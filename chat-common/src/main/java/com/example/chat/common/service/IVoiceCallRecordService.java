package com.example.chat.common.service;

import com.example.chat.common.entity.VoiceCallRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface IVoiceCallRecordService extends IService<VoiceCallRecord> {
    VoiceCallRecord startCall(Long calleeId, Byte callType, Integer groupId);
    void answerCall(Long callId);
    void endCall(Long callId);
    void rejectCall(Long callId);
    List<VoiceCallRecord> callHistory(Long withUserId);
    int callCount(Long withUserId);
    long totalDuration(Long withUserId);
}
