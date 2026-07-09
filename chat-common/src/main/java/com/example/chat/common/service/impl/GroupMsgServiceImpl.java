package com.example.chat.common.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.chat.common.entity.GroupMember;
import com.example.chat.common.entity.GroupMsg;
import com.example.chat.common.mapper.GroupMsgMapper;
import com.example.chat.common.service.IGroupMemberService;
import com.example.chat.common.service.IGroupMsgService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupMsgServiceImpl extends ServiceImpl<GroupMsgMapper, GroupMsg>
        implements IGroupMsgService {

    @Resource private IGroupMemberService groupMemberService;

    @Override
    public GroupMsg sendMsg(Integer groupId, Byte msgType, String content,
                            String fileUrl, String fileName, Double fileSize) {
        long loginId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<GroupMember> qw = new LambdaQueryWrapper<>();
        qw.eq(GroupMember::getGroupId, groupId.longValue()).eq(GroupMember::getUserId, loginId);
        if (groupMemberService.getOne(qw) == null) throw new RuntimeException("不在群中");

        GroupMsg msg = new GroupMsg(); msg.setGroupId(groupId); msg.setFromUserId((int) loginId);
        msg.setMsgType(msgType); msg.setContent(content);
        msg.setFileUrl(fileUrl); msg.setFileName(fileName); msg.setFileSize(fileSize);
        msg.setMsgIsRecalled((byte) 0); msg.setMsgSendTime(LocalDateTime.now());
        this.save(msg); return msg;
    }

    @Override
    public List<GroupMsg> history(Integer groupId, int page, int size) {
        LambdaQueryWrapper<GroupMsg> qw = new LambdaQueryWrapper<>();
        qw.eq(GroupMsg::getGroupId, groupId).orderByDesc(GroupMsg::getMsgSendTime);
        return this.page(new Page<>(page, size), qw).getRecords();
    }

    @Override
    public void recallMsg(Integer msgId) {
        long loginId = StpUtil.getLoginIdAsLong();
        GroupMsg msg = this.getById(msgId);
        if (msg == null) throw new RuntimeException("消息不存在");
        if (!msg.getFromUserId().equals((int) loginId)) throw new RuntimeException("只能撤回自己的消息");
        msg.setMsgIsRecalled((byte) 1); msg.setMsgRecallTime(LocalDateTime.now()); this.updateById(msg);
    }
}
