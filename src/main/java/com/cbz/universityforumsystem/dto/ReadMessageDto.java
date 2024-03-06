package com.cbz.universityforumsystem.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReadMessageDto {

    private short type;

    @NotBlank
    private String idList;
}
