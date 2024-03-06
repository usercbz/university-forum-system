package com.cbz.universityforumsystem.controller;

import com.cbz.universityforumsystem.dto.ReplyLikeDto;
import com.cbz.universityforumsystem.dto.ReplySaveDto;
import com.cbz.universityforumsystem.service.CommentReplyService;
import com.cbz.universityforumsystem.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reply")
public class CommentReplyController {

    @Autowired
    private CommentReplyService commentReplyService;

    @GetMapping("load")
    public Result loadReplies(@RequestParam String commentId) {
        return commentReplyService.loadReplies(commentId);
    }

    @PostMapping("save")
    public Result saveReply(@Validated @RequestBody ReplySaveDto replySaveDto) {

        return commentReplyService.saveReply(replySaveDto);
    }

    @PutMapping("like")
    public Result handleLike(@Validated @RequestBody ReplyLikeDto replyLikeDto) {
        return commentReplyService.handleLike(replyLikeDto);
    }
}
