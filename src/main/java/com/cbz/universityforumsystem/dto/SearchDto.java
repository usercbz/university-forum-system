package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SearchDto {

    @NotBlank(message = "搜索内容为空")
    private String content;

    private Integer page;

    private Integer pageSize;
}
