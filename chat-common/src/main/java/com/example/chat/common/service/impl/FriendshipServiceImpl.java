package com.example.chat.common.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.chat.common.entity.Friendship;
import com.example.chat.common.entity.User;
import com.example.chat.common.mapper.FriendshipMapper;
import com.example.chat.common.service.IFriendshipService;
import com.example.chat.common.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendshipServiceImpl extends ServiceImpl<FriendshipMapper, Friendship>
        implements IFriendshipService {

    @Resource
    private IUserService userService;

    private void populateNickNames(List<Friendship> list) {
        if (list.isEmpty()) return;
        Set<Integer> friendIds = list.stream().map(f -> f.getFriendId().intValue()).collect(Collectors.toSet());
        Map<Integer, String> nickMap = userService.listByIds(friendIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u.getNickName() != null ? u.getNickName() : ""));
        list.forEach(f -> {
            if (f.getFriendRemark() == null || f.getFriendRemark().isEmpty()) {
                f.setFriendRemark(nickMap.getOrDefault(f.getFriendId().intValue(), ""));
            }
            f.setFriendNickName(nickMap.getOrDefault(f.getFriendId().intValue(), ""));
        });
    }

    @Override
    public List<Friendship> friendList() {
        long loginId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<Friendship> qw = new LambdaQueryWrapper<>();
        qw.eq(Friendship::getUserId, loginId);
        List<Friendship> list = this.list(qw);
        populateNickNames(list);
        return list;
    }

    @Override
    public List<Friendship> friendListByCategory(String category) {
        long loginId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<Friendship> qw = new LambdaQueryWrapper<>();
        qw.eq(Friendship::getUserId, loginId).eq(Friendship::getCategory, category);
        List<Friendship> list = this.list(qw);
        populateNickNames(list);
        return list;
    }

    @Override
    public List<String> friendCategories() {
        return List.of("朋友", "家人", "同学");
    }

    @Override
    public void deleteFriend(Long friendId) {
        long loginId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<Friendship> qw = new LambdaQueryWrapper<>();
        qw.eq(Friendship::getUserId, loginId).eq(Friendship::getFriendId, friendId);
        this.remove(qw);
        qw = new LambdaQueryWrapper<>();
        qw.eq(Friendship::getUserId, friendId).eq(Friendship::getFriendId, loginId);
        this.remove(qw);
    }
}
