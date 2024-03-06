package com.cbz.universityforumsystem.controller;

import com.cbz.universityforumsystem.dto.CommentDto;
import com.cbz.universityforumsystem.dto.CommentLikeDto;
import com.cbz.universityforumsystem.dto.CommentSaveDto;
import com.cbz.universityforumsystem.service.CommentService;
import com.cbz.universityforumsystem.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("load")
    public Result loadComment(@Validated @RequestBody CommentDto commentDto) {
        return commentService.getComments(commentDto);
    }

    @PutMapping("like")
    public Result handleLike(@Validated @RequestBody CommentLikeDto commentLikeDto) {
        return commentService.handleLike(commentLikeDto);
    }

    @PostMapping("save")
    public Result saveComment(@Validated @RequestBody CommentSaveDto commentSaveDto) {
        return commentService.saveComment(commentSaveDto);
    }
}
