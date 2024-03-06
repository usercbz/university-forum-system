package com.cbz.universityforumsystem.entity;

import lombok.Data;


@Data
public class SearchArticle {

    //文章id
    private String id;
    //作者id
    private String authorId;
    //标题
    private String title;
    //内容
    private String content;
    //封面
    private String cover;
    //发布时间
    private String publishTime;
}
