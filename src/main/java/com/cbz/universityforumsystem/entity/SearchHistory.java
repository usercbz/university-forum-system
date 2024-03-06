package com.cbz.universityforumsystem.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document
public class SearchHistory {
    //id
    private String id;
    //用户ID
    private Long userId;
    //搜索内容
    private String content;
    //时间
    private LocalDateTime createTime;
}
