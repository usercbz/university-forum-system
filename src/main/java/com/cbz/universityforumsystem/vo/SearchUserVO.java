package com.cbz.universityforumsystem.vo;

import com.cbz.universityforumsystem.entity.SearchUser;
import lombok.Data;

@Data
public class SearchUserVO extends SearchUser {

    //粉丝数
    private Integer fanNumber;
    //文章数
    private Integer articleNumber;
    //释放关注
    private boolean isFollow;

}
