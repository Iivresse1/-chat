package com.example.chat.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.chat.common.entity.GroupMember;
import com.example.chat.common.mapper.GroupMemberMapper;
import com.example.chat.common.service.IGroupMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, GroupMember> implements IGroupMemberService {

    @Override
    public List<GroupMember> joinedGroups(Long userId) {
        LambdaQueryWrapper<GroupMember> qw = new LambdaQueryWrapper<>();
        qw.eq(GroupMember::getUserId, userId);
        return this.list(qw);
    }

    @Override
    public List<GroupMember> managedGroups(Long userId) {
        LambdaQueryWrapper<GroupMember> qw = new LambdaQueryWrapper<>();
        qw.eq(GroupMember::getUserId, userId).in(GroupMember::getGroupRole, List.of((byte) 1, (byte) 2));
        return this.list(qw);
    }
}
