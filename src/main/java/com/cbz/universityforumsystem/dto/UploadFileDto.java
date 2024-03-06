package com.cbz.universityforumsystem.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
public class UploadFileDto {

    //上传者
    @NotNull(message = "非法访问")
    private Long uploadUser;
    //文件
    @NotNull(message = "上传文件为空！")
    private MultipartFile file;
}
