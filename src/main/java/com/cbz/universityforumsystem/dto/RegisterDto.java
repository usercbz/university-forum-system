package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class RegisterDto {

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Email(message = "请书写正确的邮箱格式")
    private String email;

    @NotBlank(message = "验证码不能为空")
    private String checkCode;
}
