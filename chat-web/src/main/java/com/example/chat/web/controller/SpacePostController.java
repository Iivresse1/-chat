package com.example.chat.web.controller;

import com.example.chat.common.Result;
import com.example.chat.common.service.IUserSpacePostService;
import com.example.chat.web.dto.PublishPostDto;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/space")
public class SpacePostController {

    @Resource
    private IUserSpacePostService spacePostService;

    @PostMapping("/publish")
    public Result publish(@RequestBody PublishPostDto dto) {
        return Result.success(spacePostService.createPost(
            dto.getContent(), dto.getImages(), dto.getLocation(), dto.getStatus()
        ));
    }

    @GetMapping("/my-posts")
    public Result myPosts(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size) {
        return Result.success(spacePostService.myPosts(page, size));
    }

    @GetMapping("/friend-posts")
    public Result friendPosts(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return Result.success(spacePostService.friendPosts(page, size));
    }

    @GetMapping("/user-posts")
    public Result userPosts(@RequestParam Integer userId,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "20") int size) {
        return Result.success(spacePostService.userPosts(userId, page, size));
    }

    @PostMapping("/like")
    public Result like(@RequestBody Map<String, Object> params) {
        Integer postId = Integer.valueOf(params.get("postId").toString());
        spacePostService.likePost(postId);
        return Result.success();
    }

    @PostMapping("/comment")
    public Result comment(@RequestBody Map<String, Object> params) {
        Integer postId = Integer.valueOf(params.get("postId").toString());
        spacePostService.commentPost(postId);
        return Result.success();
    }

    @PostMapping("/view")
    public Result view(@RequestBody Map<String, Object> params) {
        Integer postId = Integer.valueOf(params.get("postId").toString());
        spacePostService.incrementViewCount(postId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        spacePostService.deletePost(id);
        return Result.success();
    }
}
