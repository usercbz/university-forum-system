package com.cbz.universityforumsystem.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class UserFanVO {
    //用户id
    private Long userId;
    //昵称
    private String nickname;
    //头像
    private String avatar;
    //已关注
    private boolean isFollow;
    //是粉丝
    private boolean isFan;
    //关系创建时间
    private LocalDateTime createTime;
}
