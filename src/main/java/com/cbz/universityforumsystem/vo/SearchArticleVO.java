package com.cbz.universityforumsystem.vo;

import lombok.Data;


@Data
public class SearchArticleVO {
    //文章id
    private String id;
    //作者id
    private String authorId;
    //作者头像
    private String authorAvatar;
    //作者名称
    private String authorName;
    //标题
    private String title;
    //封面
    private String cover;
    //发布时间
    private String publishTime;
}
