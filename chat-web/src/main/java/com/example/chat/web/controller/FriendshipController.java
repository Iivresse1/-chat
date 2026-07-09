package com.example.chat.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.chat.common.Result;
import com.example.chat.common.service.IFriendshipService;
import com.example.chat.common.service.IGroupMemberService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/friendship")
public class FriendshipController {

    @Resource
    private IFriendshipService friendshipService;

    @Resource
    private IGroupMemberService groupMemberService;

    @GetMapping("/list")
    public Result list() {
        return Result.success(friendshipService.friendList());
    }

    @GetMapping("/by-category")
    public Result listByCategory(@RequestParam String category) {
        return Result.success(friendshipService.friendListByCategory(category));
    }

    @GetMapping("/categories")
    public Result categories() {
        return Result.success(friendshipService.friendCategories());
    }

    /** 好友列表分组视图：分类好友 + 群聊 */
    @GetMapping("/groups-view")
    public Result groupsView() {
        long userId = StpUtil.getLoginIdAsLong();
        List<Map<String, Object>> groups = new java.util.ArrayList<>();

        // 朋友分组
        for (String cat : friendshipService.friendCategories()) {
            var friends = friendshipService.friendListByCategory(cat);
            if (!friends.isEmpty()) {
                groups.add(Map.of("type", "friend", "name", cat, "count", friends.size()));
            }
        }

        // 群聊分组
        var joined = groupMemberService.joinedGroups(userId);
        var managed = groupMemberService.managedGroups(userId);
        if (!joined.isEmpty()) {
            groups.add(Map.of("type", "group", "name", "我加入的群聊", "count", joined.size()));
        }
        if (!managed.isEmpty()) {
            groups.add(Map.of("type", "group", "name", "我管理的群聊", "count", managed.size()));
        }

        return Result.success(groups);
    }

    @DeleteMapping("/delete/{friendId}")
    public Result deleteFriend(@PathVariable Long friendId) {
        friendshipService.deleteFriend(friendId);
        return Result.success();
    }
}
