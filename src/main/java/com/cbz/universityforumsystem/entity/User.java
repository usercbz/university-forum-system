package com.cbz.universityforumsystem.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class User {

    public User() {
    }

    public User(Long id, String account, String password, String nickname, String salt, Short sex, String email, String avatar, String description, Short status, Short permission, LocalDateTime createTime) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.nickname = nickname;
        this.salt = salt;
        this.sex = sex;
        this.email = email;
        this.avatar = avatar;
        this.description = description;
        this.status = status;
        this.permission = permission;
        this.createTime = createTime;
    }

    //id
    private Long id;
    //账号
    private String account;
    //密码
    private String password;
    //昵称
    private String nickname;
    //盐
    private String salt;
    //性别 0- 女 1- 男
    private Short sex;
    //邮箱
    private String email;
    //头像
    private String avatar;
    //简介
    private String description;
    //状态 0 - 异常  1 - 正常
    private Short status;
    //权限  0 - 普通用户  1 - 管理员
    private Short permission;
    //创建时间
    private LocalDateTime createTime;
}
