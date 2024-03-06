package com.cbz.universityforumsystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    //id
    private Long id;
    //账号
    private String account;
    //昵称
    private String nickname;
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
