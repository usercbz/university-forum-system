package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class CommentDto {

    @NotNull(message = "缺少参数")
    private Long articleId;

    private LocalDateTime minTime;

    private Integer pageSize;
}
