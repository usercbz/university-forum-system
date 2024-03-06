package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class HandleFollowDto {

    @NotNull(message = "非法操作")
    private Long followId;

    // -1 - 取关  1 - 关注
    private short option;

}
