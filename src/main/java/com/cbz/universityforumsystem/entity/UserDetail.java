package com.cbz.universityforumsystem.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class UserDetail {
    @TableId
    private Long userId;
    private int articleNumber;
    private int followNumber;
    private int fanNumber;
}
