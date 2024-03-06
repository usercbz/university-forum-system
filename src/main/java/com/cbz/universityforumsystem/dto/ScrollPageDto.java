package com.cbz.universityforumsystem.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScrollPageDto {

    //最小时间
    private LocalDateTime minTime;
    //分页大小
    private Long pageSize;
}
