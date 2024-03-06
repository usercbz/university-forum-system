package com.cbz.universityforumsystem.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("comment_like")
public class CommentLike {

    private String id;

    private String commentId;

    private Long userId;
}
