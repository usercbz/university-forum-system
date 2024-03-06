package com.cbz.universityforumsystem.controller;

import com.cbz.universityforumsystem.dto.SearchDto;
import com.cbz.universityforumsystem.service.SearchService;
import com.cbz.universityforumsystem.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping("article")
    public Result searchArticle(@Validated @RequestBody SearchDto searchDto) {
        return searchService.searchArticle(searchDto);
    }

    @PostMapping("user")
    public Result searchUser(@Validated @RequestBody SearchDto searchDto){
        return searchService.searchUser(searchDto);
    }
}
