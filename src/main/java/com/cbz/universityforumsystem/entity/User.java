package com.cbz.universityforumsystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String account;
    private String password;
    private String nickname;
    private String salt;
    private short sex;
    private String email;
    private String avatar;
    private String description;
    private short status;
    private short permission;
    private LocalDateTime createTime;
}
