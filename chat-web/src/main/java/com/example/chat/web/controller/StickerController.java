package com.example.chat.web.controller;

import com.example.chat.common.Result;
import com.example.chat.common.service.IUserStickerService;
import com.example.chat.web.dto.AddStickerDto;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sticker")
public class StickerController {

    @Resource
    private IUserStickerService stickerService;

    @PostMapping("/add")
    public Result add(@RequestBody AddStickerDto dto) {
        stickerService.addSticker(dto.getStickerUrl(), dto.getStickerName(), dto.getType());
        return Result.success();
    }

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Byte type) {
        return Result.success(stickerService.myStickers(type));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        stickerService.deleteSticker(id);
        return Result.success();
    }
}
