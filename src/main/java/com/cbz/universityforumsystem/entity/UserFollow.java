package com.cbz.universityforumsystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserFollow {
    private Long id;
    private Long followId;
    private Long userId;
    private LocalDateTime createTime;
}
