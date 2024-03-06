package com.cbz.universityforumsystem.vo;

import com.cbz.universityforumsystem.entity.CommentReply;
import lombok.Data;

@Data
public class CommentReplyVO extends CommentReply {

    private boolean isLiked;
}
