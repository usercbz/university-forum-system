package com.cbz.universityforumsystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Article {
    //id
    private Long id;
    //作者id
    private Long authorId;
    //作者名称
    private String authorName;
    //标题
    private String title;
    //图片
    private String images;
    //谁可见  0 -所有人   1 -好友可见   2 -仅自己可见
    private Short visible;
    //发布时间
    private LocalDateTime publishTime;
}
