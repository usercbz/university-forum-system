package com.cbz.universityforumsystem.vo;

import lombok.Data;

@Data
public class UserDetailVO {

    //用户id
    private Long id;
    //昵称
    private String nickname;
    //头像
    private String avatar;
    //账号
    private String account;
    //用户简介
    private String description;
    //用户性别
    private Short sex;
    //文章数量
    private int articleNumber;
    //关注数量
    private int followNumber;
    //粉丝数量
    private int fanNumber;
    //已关注
    private boolean isFollow;
}
