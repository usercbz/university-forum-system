package com.cbz.universityforumsystem.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("reply_like")
public class ReplyLike {

    private String id;

    private String replyId;

    private Long userId;
}
