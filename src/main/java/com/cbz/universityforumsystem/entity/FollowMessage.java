package com.cbz.universityforumsystem.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("follow_message")
public class FollowMessage {

    //ID
    private String id;
    //作者id
    private Long authorId;
    //粉丝ID
    private Long fanId;
    //粉丝昵称
    private String fanNickname;
    //粉丝头像
    private String fanAvatar;
    //创建时间
    private LocalDateTime updateTime;
}
