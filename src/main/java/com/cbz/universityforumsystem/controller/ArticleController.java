package com.cbz.universityforumsystem.controller;

import com.cbz.universityforumsystem.dto.ArticleDto;
import com.cbz.universityforumsystem.dto.HandleArticleDto;
import com.cbz.universityforumsystem.dto.PublishArticleDto;
import com.cbz.universityforumsystem.service.ArticleService;
import com.cbz.universityforumsystem.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 加载文章
     *
     * @param minTime
     * @param pageSize
     * @return 文章数据
     */
    @GetMapping("list")
    public Result getArticles(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                              @RequestParam(required = false) LocalDateTime minTime,
                              @RequestParam(defaultValue = "20") Long pageSize) {
        return articleService.loadArticles(minTime, pageSize, false);
    }

    /**
     * 加载关注文章
     *
     * @param minTime
     * @param pageSize
     * @return 关注文章
     */
    @GetMapping("/list/follow")
    public Result getFollowArticles(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                    @RequestParam(required = false) LocalDateTime minTime,
                                    @RequestParam(defaultValue = "20") Long pageSize) {
        return articleService.loadArticles(minTime, pageSize, true);
    }

    /**
     * 获取我的文章
     *
     * @param minTime
     * @param pageSize
     * @return 我的文章
     */
    @GetMapping("my")
    public Result getMyArticles(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                @RequestParam(required = false) LocalDateTime minTime,
                                @RequestParam(defaultValue = "18") Long pageSize) {
        return articleService.getMyArticles(minTime, pageSize);
    }

    /**
     * 我的收藏
     *
     * @param minTime
     * @param pageSize
     * @return 收藏文章
     */
    @GetMapping("/my/collect")
    public Result getMyCollect(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                               @RequestParam(required = false) LocalDateTime minTime,
                               @RequestParam(defaultValue = "18") Long pageSize) {
//        log.info("minTime:{},pageSize:{}", minTime, pageSize);
        return articleService.getMyCollect(minTime, pageSize);
    }

    /**
     * 加载热点文章数据
     *
     * @return 热点文章
     */
    @GetMapping("hot")
    public Result getHotArticles() {
        return articleService.getHotArticles();
    }

    /**
     * 发布文章
     *
     * @param publishArticleDto
     * @return
     */
    @PostMapping("publish")
    public Result publishArticle(@Validated @RequestBody PublishArticleDto publishArticleDto) {
//        log.info("{}", publishArticleDto);
        return articleService.publish(publishArticleDto);
    }

    //收藏文章、取消收藏
    @PutMapping("/handle/collect")
    public Result collectArticle(@Validated @RequestBody HandleArticleDto handleArticleDto) {
        return articleService.handleArticleCollect(handleArticleDto);
    }

    //文章编辑
    @PutMapping
    public Result editArticle(@Validated @RequestBody ArticleDto articleDto) {
        return articleService.editArticle(articleDto);
    }

    //文章下架、上架
    @PutMapping("/handle/status")
    public Result handleStatus(@Validated @RequestBody HandleArticleDto handleArticleDto) {
        return articleService.handleStatus(handleArticleDto);
    }

    //文章点赞、取消
    @PutMapping("/handle/like")
    public Result handleLike(@Validated @RequestBody HandleArticleDto handleArticleDto) {
        return articleService.handleLike(handleArticleDto);
    }

    @DeleteMapping("/{articleId}")
    public Result deleteArticle(@PathVariable Long articleId) {
        return articleService.deleteArticleById(articleId);
    }

    @GetMapping("/detail/{articleId}")
    public Result getArticleDetail(@PathVariable Long articleId) {
        return articleService.getArticleDetail(articleId);
    }

    @GetMapping("/list/user/{userId}")
    public Result getArticleByUserId(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                     @RequestParam(required = false) LocalDateTime minTime,
                                     @RequestParam(defaultValue = "18") Long pageSize,
                                     @PathVariable Long userId) {
        return articleService.getArticleByUserId(userId,minTime,pageSize);
    }
}
