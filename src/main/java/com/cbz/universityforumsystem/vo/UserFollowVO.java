package com.cbz.universityforumsystem.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class UserFollowVO {

    //用户id
    private Long userId;
    //昵称
    private String nickname;
    //头像
    private String avatar;
    //文章数量
    private int articleNumber;
    //粉丝数量
    private int fanNumber;
    //已关注
    private boolean isFollow;
    //是粉丝
    private boolean isFan;
    //关系创建时间
    private LocalDateTime createTime;
}
