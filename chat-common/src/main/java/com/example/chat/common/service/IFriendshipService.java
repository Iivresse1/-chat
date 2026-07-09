package com.example.chat.common.service;

import com.example.chat.common.entity.Friendship;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface IFriendshipService extends IService<Friendship> {
    List<Friendship> friendList();
    List<Friendship> friendListByCategory(String category);
    List<String> friendCategories();
    void deleteFriend(Long friendId);
}
