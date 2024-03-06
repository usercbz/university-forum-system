package com.cbz.universityforumsystem.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("comment_reply")
public class CommentReply {
    //回复ID
    private String id;
    //评论ID
    private String commentId;
    //目标类型 0 -- 评论  1-- 回复
    private short targetType;
    //目标用户id
    private Long targetId;
    //目标昵称
    private String targetName;
    //用户ID
    private Long userId;
    //用户头像
    private String userAvatar;
    //用户昵称
    private String nickname;
    //回复内容
    private String content;
    //点赞数
    private Long likeNumber;
    //发布时间
    private LocalDateTime publishTime;
}
