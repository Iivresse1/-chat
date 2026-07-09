package com.example.chat.web.vo;

import com.example.chat.common.entity.VoiceCallRecord;
import lombok.Data;

import java.util.List;

@Data
public class CallStatsVo {
    private int callCount;
    private long totalDuration;                 // 总时长（秒）
    private List<VoiceCallRecord> records;      // 通话明细
}
