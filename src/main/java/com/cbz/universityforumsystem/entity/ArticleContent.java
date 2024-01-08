package com.cbz.universityforumsystem.entity;

import lombok.Data;

@Data
public class ArticleContent {
    //id
    private Long id;
    //文章id
    private Long articleId;
    //内容
    private String content;
}
