package com.cbz.universityforumsystem.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("like_message")
public class LikeMessage {

    //ID
    private String id;
    //作者ID
    private Long authorId;
    //实体ID（文章/评论/回复ID）
    private Object entityId;
    //实体内容（文章标题/评论/回复内容）
    private String entityContent;
    //类型（0 文章 1 评论）
    private short type;
    //操作用户ID
    private Long userId;
    //用户昵称
    private String nickname;
    //点赞次数
    private long likeNumber;
    //操作时间
    private LocalDateTime updateTime;
}
