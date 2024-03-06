package com.cbz.universityforumsystem.vo;

import com.cbz.universityforumsystem.entity.Comment;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class CommentVO extends Comment {

    private boolean isLiked;

    private  List<?> replies = Collections.emptyList();
}
