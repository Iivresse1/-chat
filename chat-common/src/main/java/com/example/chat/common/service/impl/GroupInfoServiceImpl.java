package com.example.chat.common.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.chat.common.entity.GroupInfo;
import com.example.chat.common.entity.GroupMember;
import com.example.chat.common.mapper.GroupInfoMapper;
import com.example.chat.common.service.IGroupInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.chat.common.service.IGroupMemberService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo>
        implements IGroupInfoService {

    @Resource private IGroupMemberService groupMemberService;

    @Override
    public GroupInfo createGroup(String groupName) {
        long loginId = StpUtil.getLoginIdAsLong();
        GroupInfo group = new GroupInfo();
        group.setGroupName(groupName); group.setOwnerId(loginId);
        group.setMaxMembers(200); group.setStatus((byte) 0);
        group.setCreateTime(LocalDateTime.now()); group.setMemberCount(1);
        this.save(group);

        GroupMember owner = new GroupMember();
        owner.setGroupId(group.getId().longValue()); owner.setUserId(loginId);
        owner.setGroupRole((byte) 2); owner.setGroupStatus((byte) 0);
        owner.setGroupJoinTime(LocalDateTime.now());
        groupMemberService.save(owner);
        return group;
    }

    @Override
    public void joinGroup(Integer groupId) {
        long loginId = StpUtil.getLoginIdAsLong();
        GroupInfo group = this.getById(groupId);
        if (group == null || group.getStatus() != 0) throw new RuntimeException("群不存在或已解散");

        LambdaQueryWrapper<GroupMember> qw = new LambdaQueryWrapper<>();
        qw.eq(GroupMember::getGroupId, groupId.longValue()).eq(GroupMember::getUserId, loginId);
        if (groupMemberService.getOne(qw) != null) throw new RuntimeException("已在群中");

        GroupMember m = new GroupMember(); m.setGroupId(groupId.longValue()); m.setUserId(loginId);
        m.setGroupRole((byte) 0); m.setGroupStatus((byte) 0); m.setGroupJoinTime(LocalDateTime.now());
        groupMemberService.save(m);
        group.setMemberCount(group.getMemberCount() + 1); this.updateById(group);
    }

    @Override
    public void leaveGroup(Integer groupId) {
        long loginId = StpUtil.getLoginIdAsLong();
        GroupInfo group = this.getById(groupId);
        LambdaQueryWrapper<GroupMember> qw = new LambdaQueryWrapper<>();
        qw.eq(GroupMember::getGroupId, groupId.longValue()).eq(GroupMember::getUserId, loginId);
        GroupMember member = groupMemberService.getOne(qw);
        if (member == null) throw new RuntimeException("不在群中");
        if (member.getGroupRole() == (byte) 2) throw new RuntimeException("群主不能退出");
        groupMemberService.removeById(member.getId());
        group.setMemberCount(group.getMemberCount() - 1); this.updateById(group);
    }

    @Override
    public void updateGroupName(Integer groupId, String groupName) {
        long loginId = StpUtil.getLoginIdAsLong();
        GroupInfo group = this.getById(groupId);
        if (!group.getOwnerId().equals(loginId)) throw new RuntimeException("只有群主能改名");
        group.setGroupName(groupName); this.updateById(group);
    }

    @Override
    public void dismissGroup(Integer groupId) {
        long loginId = StpUtil.getLoginIdAsLong();
        GroupInfo group = this.getById(groupId);
        if (!group.getOwnerId().equals(loginId)) throw new RuntimeException("只有群主能解散");
        group.setStatus((byte) 1); this.updateById(group);
    }

    @Override
    public List<GroupMember> memberList(Integer groupId) {
        LambdaQueryWrapper<GroupMember> qw = new LambdaQueryWrapper<>();
        qw.eq(GroupMember::getGroupId, groupId.longValue());
        return groupMemberService.list(qw);
    }
}
