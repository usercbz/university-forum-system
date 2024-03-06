package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class CodeDto {
    @Email(message = "请输入正确邮箱格式")
    private  String email;

    private short subject;
}
