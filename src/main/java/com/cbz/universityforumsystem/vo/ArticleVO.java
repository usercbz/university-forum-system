package com.cbz.universityforumsystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleVO {

    //文章ID
    private Long id;
    //文章标题
    private String title;
    //文章内容
    private String content;
    //文章封面
    private String cover;
    //文章作者ID
    private Long authorId;
    //文章作者昵称
    private String authorName;
    //点赞数
    private Object likeNumber;
    //收藏数
    private Object collectionNumber;
    //评论数
    private Object commentNumber;
    //是否点赞
    private boolean isLiked;
    //是否收藏
    private boolean isCollected;
    //发布时间
    private LocalDateTime publishTime;

}
