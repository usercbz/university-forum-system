package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReplyLikeDto {

    @NotBlank(message = "缺少参数")
    private String replyId;

    private short option;
}
