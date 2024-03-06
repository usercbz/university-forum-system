package com.cbz.universityforumsystem.controller;

import com.cbz.universityforumsystem.dto.UploadFileDto;
import com.cbz.universityforumsystem.service.FileService;
import com.cbz.universityforumsystem.utils.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload/image")
    public Result uploadImage( @Validated UploadFileDto fileDto){
        return fileService.uploadImage(fileDto);
    }
}
