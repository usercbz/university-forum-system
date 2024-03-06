package com.cbz.universityforumsystem.entity;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class UserCollect {
    private Long id;
    private Long articleId;
    private Long userId;
    private LocalDateTime createTime;
}
