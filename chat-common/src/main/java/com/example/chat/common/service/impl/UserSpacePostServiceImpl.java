package com.example.chat.common.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.chat.common.entity.Friendship;
import com.example.chat.common.entity.User;
import com.example.chat.common.entity.UserSpacePost;
import com.example.chat.common.mapper.UserSpacePostMapper;
import com.example.chat.common.service.IFriendshipService;
import com.example.chat.common.service.IUserService;
import com.example.chat.common.service.IUserSpacePostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserSpacePostServiceImpl extends ServiceImpl<UserSpacePostMapper, UserSpacePost>
        implements IUserSpacePostService {

    @Resource private IFriendshipService friendshipService;
    @Resource private IUserService userService;

    private void populateUserInfo(List<UserSpacePost> list) {
        if (list.isEmpty()) return;
        Set<Integer> userIds = list.stream().map(p -> p.getUserId().intValue()).collect(Collectors.toSet());
        Map<Integer, User> userMap = userService.listByIds(userIds).stream()
            .collect(Collectors.toMap(User::getId, u -> u));
        list.forEach(p -> {
            User u = userMap.get(p.getUserId().intValue());
            if (u != null) {
                p.setUserNickName(u.getNickName());
                p.setUserAvatar(u.getAvatar());
            }
        });
    }

    @Override
    public UserSpacePost createPost(String content, String images, String location, Byte status) {
        int loginId = StpUtil.getLoginIdAsInt();
        UserSpacePost post = new UserSpacePost();
        post.setUserId((long) loginId); post.setContent(content); post.setImages(images);
        post.setLocation(location); post.setStatus(status);
        post.setViewCount(0); post.setLikeCount(0); post.setCommentCount(0);
        post.setCreateTime(LocalDateTime.now());
        this.save(post); return post;
    }

    @Override
    public List<UserSpacePost> myPosts(int page, int size) {
        int loginId = StpUtil.getLoginIdAsInt();
        LambdaQueryWrapper<UserSpacePost> qw = new LambdaQueryWrapper<>();
        qw.eq(UserSpacePost::getUserId, (long) loginId).orderByDesc(UserSpacePost::getCreateTime);
        List<UserSpacePost> list = this.page(new Page<>(page, size), qw).getRecords();
        populateUserInfo(list);
        return list;
    }

    @Override
    public List<UserSpacePost> friendPosts(int page, int size) {
        int loginId = StpUtil.getLoginIdAsInt();
        LambdaQueryWrapper<Friendship> fw = new LambdaQueryWrapper<>();
        fw.eq(Friendship::getUserId, (long) loginId);
        List<Long> friendIds = friendshipService.list(fw).stream().map(Friendship::getFriendId).collect(Collectors.toList());
        if (friendIds.isEmpty()) return List.of();
        LambdaQueryWrapper<UserSpacePost> qw = new LambdaQueryWrapper<>();
        qw.in(UserSpacePost::getUserId, friendIds).eq(UserSpacePost::getStatus, (byte) 0)
          .orderByDesc(UserSpacePost::getCreateTime);
        List<UserSpacePost> list = this.page(new Page<>(page, size), qw).getRecords();
        populateUserInfo(list);
        return list;
    }

    @Override
    public List<UserSpacePost> userPosts(Integer userId, int page, int size) {
        LambdaQueryWrapper<UserSpacePost> qw = new LambdaQueryWrapper<>();
        qw.eq(UserSpacePost::getUserId, userId.longValue()).eq(UserSpacePost::getStatus, (byte) 0)
          .orderByDesc(UserSpacePost::getCreateTime);
        List<UserSpacePost> list = this.page(new Page<>(page, size), qw).getRecords();
        populateUserInfo(list);
        return list;
    }

    @Override public void likePost(Integer postId) { UserSpacePost p = this.getById(postId.longValue()); if (p != null) { p.setLikeCount(p.getLikeCount() + 1); this.updateById(p); } }
    @Override public void commentPost(Integer postId) { UserSpacePost p = this.getById(postId.longValue()); if (p != null) { p.setCommentCount(p.getCommentCount() + 1); this.updateById(p); } }
    @Override public void incrementViewCount(Integer postId) { UserSpacePost p = this.getById(postId.longValue()); if (p != null) { p.setViewCount(p.getViewCount() + 1); this.updateById(p); } }

    @Override
    public void deletePost(Integer postId) {
        int loginId = StpUtil.getLoginIdAsInt();
        UserSpacePost post = this.getById(postId.longValue());
        if (post == null) throw new RuntimeException("动态不存在");
        if (!post.getUserId().equals((long) loginId)) throw new RuntimeException("只能删除自己的动态");
        this.removeById(postId.longValue());
    }
}
