package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PublishArticleDto {

    //标题
    @NotBlank(message = "标题不能为空")
    private String title;
    //内容
    @NotBlank(message = "内容不能为空")
    private String content;
    //逗号隔开
    private String images;
    //封面
    private String cover;
    //谁可看
    private Short visible;

}
