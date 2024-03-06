package com.cbz.universityforumsystem.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("comment")
public class Comment {
    //评论ID
    private String id;
    //文章ID
    private Long articleId;
    //评论用户ID
    private Long userId;
    //用户昵称
    private String nickname;
    //用户头像
    private String userAvatar;
    //评论内容
    private String content;
    //点赞数
    private Long likeNumber;
    //回复数
    private Long replyNumber;
    //发布时间
    private LocalDateTime publishTime;
}
