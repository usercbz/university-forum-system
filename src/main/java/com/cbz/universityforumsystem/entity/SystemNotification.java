package com.cbz.universityforumsystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemNotification {
    //id
    private Long id;
    //标题
    private String title;
    //内容
    private String content;
    //发布时间
    private LocalDateTime publishTime;
}
