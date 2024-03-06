package com.cbz.universityforumsystem.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class ArticleBehavior {
    //文章ID
    @TableId
    private Long articleId;
    //浏览次数
    private int browseNumber;
    //点餐次数
    private int likeNumber;
    //评论数
    private int commentNumber;
    //收藏数
    private int collectionNumber;
}
