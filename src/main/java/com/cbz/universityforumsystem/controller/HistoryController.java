package com.cbz.universityforumsystem.controller;

import com.cbz.universityforumsystem.service.SearchHistoryService;
import com.cbz.universityforumsystem.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private SearchHistoryService searchHistoryService;

    @GetMapping()
    public Result loadSearchHistory(){
        return searchHistoryService.loadSearchHistory();
    }

    @PostMapping
    public Result saveHistory(@NotBlank(message = "内容不能为空") @RequestParam String content){
        return searchHistoryService.saveHistory(content);
    }

    @DeleteMapping()
    public Result clearHistory(){
        return searchHistoryService.clearHistory();
    }
}
