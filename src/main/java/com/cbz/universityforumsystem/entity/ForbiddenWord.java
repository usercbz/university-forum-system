package com.cbz.universityforumsystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForbiddenWord {
    //主键id
    private Long id;
    //词
    private String word;
    //创建用户
    private Long createUser;
    //创建时间
    private LocalDateTime createTime;
}
