package com.cbz.universityforumsystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserArticleVO {

    //文章ID
    private Long id;
    //文章标题
    private String title;
    //文章封面
    private String cover;
    //是否点赞
    private boolean isLiked;
    //点赞数
    private Integer likeNumber;
    //发布时间
    private LocalDateTime publishTime;
}
