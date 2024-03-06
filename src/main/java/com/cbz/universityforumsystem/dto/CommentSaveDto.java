package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CommentSaveDto {

    @NotNull(message = "缺少参数")
    private Long articleId;

    @NotBlank(message = "内容不能为空")
    private String content;
}
