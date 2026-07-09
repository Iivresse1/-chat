package com.example.chat.common.service;

import com.example.chat.common.entity.GroupInfo;
import com.example.chat.common.entity.GroupMember;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface IGroupInfoService extends IService<GroupInfo> {
    GroupInfo createGroup(String groupName);
    void joinGroup(Integer groupId);
    void leaveGroup(Integer groupId);
    void updateGroupName(Integer groupId, String groupName);
    void dismissGroup(Integer groupId);
    List<GroupMember> memberList(Integer groupId);
}
