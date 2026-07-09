package com.example.chat.common.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.chat.common.entity.PrivateMsg;
import com.example.chat.common.mapper.PrivateMsgMapper;
import com.example.chat.common.service.IPrivateMsgService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PrivateMsgServiceImpl extends ServiceImpl<PrivateMsgMapper, PrivateMsg>
        implements IPrivateMsgService {

    @Override
    public PrivateMsg sendMsg(Integer toUserId, String msgType, String content,
                              String fileUrl, String fileName, Long fileSize) {
        long loginId = StpUtil.getLoginIdAsLong();
        PrivateMsg msg = new PrivateMsg();
        msg.setFromUserId((int) loginId); msg.setToUserId(toUserId);
        msg.setMsgType(msgType); msg.setContent(content);
        msg.setFileUrl(fileUrl); msg.setFileName(fileName); msg.setFileSize(fileSize);
        msg.setMsgStatus("sent"); msg.setMsgIsRecalled((byte) 0); msg.setMsgSendTime(LocalDateTime.now());
        this.save(msg); return msg;
    }

    @Override
    public List<PrivateMsg> history(Integer withUserId, int page, int size) {
        long loginId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<PrivateMsg> qw = new LambdaQueryWrapper<>();
        qw.and(w -> w.eq(PrivateMsg::getFromUserId, (int) loginId).eq(PrivateMsg::getToUserId, withUserId)
                .or().eq(PrivateMsg::getFromUserId, withUserId).eq(PrivateMsg::getToUserId, (int) loginId))
          .orderByDesc(PrivateMsg::getMsgSendTime);
        return this.page(new Page<>(page, size), qw).getRecords();
    }

    @Override
    public void recallMsg(Long msgId) {
        long loginId = StpUtil.getLoginIdAsLong();
        PrivateMsg msg = this.getById(msgId);
        if (msg == null) throw new RuntimeException("消息不存在");
        if (!msg.getFromUserId().equals((int) loginId)) throw new RuntimeException("只能撤回自己的消息");
        msg.setMsgIsRecalled((byte) 1); this.updateById(msg);
    }

    @Override
    public void markRead(Integer fromUserId) {
        long loginId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<PrivateMsg> qw = new LambdaQueryWrapper<>();
        qw.eq(PrivateMsg::getFromUserId, fromUserId).eq(PrivateMsg::getToUserId, (int) loginId)
          .eq(PrivateMsg::getMsgStatus, "sent");
        this.list(qw).forEach(msg -> { msg.setMsgStatus("read"); this.updateById(msg); });
    }

    @Override
    public int unreadCount(Integer fromUserId) {
        long loginId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<PrivateMsg> qw = new LambdaQueryWrapper<>();
        qw.eq(PrivateMsg::getFromUserId, fromUserId).eq(PrivateMsg::getToUserId, (int) loginId)
          .eq(PrivateMsg::getMsgStatus, "sent");
        return (int) this.count(qw);
    }
}
