package com.cbz.universityforumsystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Article {
    //id
    private Long id;
    //作者id
    private Long authorId;
    //标题
    private String title;
    //内容
    private String content;
    //图片
    private String images;
    //封面
    private String cover;
    //谁可见  0 -所有人   1 -好友可见   2 -仅自己可见
    private Short visible;
    //状态
    private Short status;
    //发布时间
    private LocalDateTime publishTime;
}
