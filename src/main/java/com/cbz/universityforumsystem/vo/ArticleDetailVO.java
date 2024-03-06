package com.cbz.universityforumsystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleDetailVO {
    //文章ID
    private Long id;
    //文章标题
    private String title;
    //文章内容
    private String content;
    //图片
    private String images;
    //文章作者ID
    private Long authorId;
    //文章作者昵称
    private String authorName;
    //作者头像
    private String authorAvatar;
    //作者简介
    private String description;
    //作者性别 0 -- 女  1--男
    private short authorSex;
    //与我的关系 0 --自己  1 --已关注  2 -- 没有建立联系
    private short relation;
    //评论数
    private Object commentNumber;
    //是否点赞
    private boolean isLiked;
    //是否收藏
    private boolean isCollected;
    //发布时间
    private LocalDateTime publishTime;
}
