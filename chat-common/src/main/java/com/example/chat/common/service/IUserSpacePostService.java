package com.example.chat.common.service;

import com.example.chat.common.entity.UserSpacePost;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface IUserSpacePostService extends IService<UserSpacePost> {
    UserSpacePost createPost(String content, String images, String location, Byte status);
    List<UserSpacePost> myPosts(int page, int size);
    List<UserSpacePost> friendPosts(int page, int size);
    List<UserSpacePost> userPosts(Integer userId, int page, int size);
    void likePost(Integer postId);
    void commentPost(Integer postId);
    void incrementViewCount(Integer postId);
    void deletePost(Integer postId);
}
