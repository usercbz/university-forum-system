package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PasswordDto {

    @NotBlank(message = "密码不能为空")
    private String oldPass;

    @NotBlank(message = "密码不能为空")
    private String newPass;
}
