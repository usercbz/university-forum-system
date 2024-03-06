package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CommentLikeDto {
    @NotBlank(message = "缺少参数")
    private String commentId;

    private short option;
}
