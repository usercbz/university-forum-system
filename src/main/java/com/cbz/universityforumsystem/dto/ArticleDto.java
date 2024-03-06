package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ArticleDto {
    //id
    @NotNull
    private Long id;
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

}
