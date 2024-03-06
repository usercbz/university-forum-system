package com.cbz.universityforumsystem.controller;

import com.cbz.universityforumsystem.dto.ReadMessageDto;
import com.cbz.universityforumsystem.service.MessageService;
import com.cbz.universityforumsystem.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/flag/message")
    public Result getMessageFlag() {
        return messageService.getMessageFlag();
    }

    @GetMapping("/flag/news")
    public Result getNewFlag() {
        return messageService.getNewFlag();
    }

    @GetMapping("like")
    public Result loadLikeMessage(@RequestParam(required = false)
                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                  LocalDateTime minTime) {
        return messageService.loadLikeMessage(minTime);
    }

    @GetMapping("reply")
    public Result loadReplyMessage(@RequestParam(required = false)
                                   @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                   LocalDateTime minTime) {
        return messageService.loadReplyMessage(minTime);
    }

    @GetMapping("sys")
    public Result loadSysMessage(@RequestParam(required = false)
                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                 LocalDateTime minTime) {
        return messageService.loadSysMessage(minTime);
    }

    @GetMapping("my")
    public Result loadMyMessage(@RequestParam(required = false)
                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                LocalDateTime minTime) {
        return messageService.loadMyMessage(minTime);
    }

}
