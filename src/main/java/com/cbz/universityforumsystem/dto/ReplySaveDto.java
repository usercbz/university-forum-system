package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReplySaveDto {
    //评论ID
    @NotBlank(message = "缺少参数")
    private String commentId;
    //目标类型 0 -- 评论  1-- 回复
    private short targetType;
    //目标ID 目标用户id
    @NotNull(message = "缺少参数")
    private Long targetId;
    //目标昵称
    @NotBlank(message = "缺少参数")
    private String targetName;
    //内容
    @NotBlank(message = "内容为空")
    private String content;
}
