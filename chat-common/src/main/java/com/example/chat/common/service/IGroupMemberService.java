package com.example.chat.common.service;

import com.example.chat.common.entity.GroupMember;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2026-06-05
 */
import java.util.List;

public interface IGroupMemberService extends IService<GroupMember> {
    List<GroupMember> joinedGroups(Long userId);
    List<GroupMember> managedGroups(Long userId);
}
