package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class HandleArticleDto {

    //文章ID
    @NotNull(message = "缺少参数")
    private Long articleId;
    //操作 1 、 0 -- 取消
    private short option;
}
