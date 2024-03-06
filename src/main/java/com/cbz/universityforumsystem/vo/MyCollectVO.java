package com.cbz.universityforumsystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MyCollectVO {

    //文章id
    private Long id;
    //作者id
    private Long authorId;
    //作者昵称
    private String authorName;
    //文章标题
    private String title;
    //文章封面
    private String cover;

    private LocalDateTime createTime;
}
