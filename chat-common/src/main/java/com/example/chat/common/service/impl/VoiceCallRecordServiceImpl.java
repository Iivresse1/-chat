package com.example.chat.common.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.chat.common.entity.VoiceCallRecord;
import com.example.chat.common.mapper.VoiceCallRecordMapper;
import com.example.chat.common.service.IVoiceCallRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoiceCallRecordServiceImpl extends ServiceImpl<VoiceCallRecordMapper, VoiceCallRecord>
        implements IVoiceCallRecordService {

    @Override
    public VoiceCallRecord startCall(Long calleeId, Byte callType, Integer groupId) {
        long loginId = StpUtil.getLoginIdAsLong();
        VoiceCallRecord r = new VoiceCallRecord();
        r.setCallerId(loginId); r.setCalleeId(calleeId);
        r.setCallType(callType); r.setGroupId(groupId); r.setStatus((byte) 0);
        this.save(r); return r;
    }

    @Override
    public void answerCall(Long callId) {
        VoiceCallRecord r = this.getById(callId);
        if (r == null) throw new RuntimeException("通话记录不存在");
        r.setStatus((byte) 1); r.setStartTime(LocalDateTime.now()); this.updateById(r);
    }

    @Override
    public void endCall(Long callId) {
        VoiceCallRecord r = this.getById(callId);
        if (r == null) throw new RuntimeException("通话记录不存在");
        LocalDateTime now = LocalDateTime.now();
        r.setStatus((byte) 2); r.setEndTime(now);
        r.setDuration(r.getStartTime() != null ? String.valueOf(Duration.between(r.getStartTime(), now).getSeconds()) : "0");
        this.updateById(r);
    }

    @Override
    public void rejectCall(Long callId) {
        VoiceCallRecord r = this.getById(callId);
        if (r == null) throw new RuntimeException("通话记录不存在");
        r.setStatus((byte) 4); this.updateById(r);
    }

    @Override
    public List<VoiceCallRecord> callHistory(Long withUserId) {
        long loginId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<VoiceCallRecord> qw = new LambdaQueryWrapper<>();
        qw.and(w -> w.eq(VoiceCallRecord::getCallerId, loginId).eq(VoiceCallRecord::getCalleeId, withUserId)
                .or().eq(VoiceCallRecord::getCallerId, withUserId).eq(VoiceCallRecord::getCalleeId, loginId))
          .eq(VoiceCallRecord::getStatus, (byte) 2).orderByDesc(VoiceCallRecord::getStartTime);
        return this.list(qw);
    }

    @Override
    public int callCount(Long withUserId) { return callHistory(withUserId).size(); }

    @Override
    public long totalDuration(Long withUserId) {
        return callHistory(withUserId).stream().mapToLong(r -> {
            try { return Long.parseLong(r.getDuration()); } catch (Exception e) { return 0; }
        }).sum();
    }
}
