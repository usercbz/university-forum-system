package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


@Data
public class RetrieveDto {

    @NotBlank(message = "账号不能为空")
    private String account;

    @Email(message = "邮箱格式有误")
    private String email;

    @NotBlank(message = "验证码为空")
    private String checkCode;

    @NotBlank(message = "密码不能为空")
    private String password;
}
