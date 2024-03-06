package com.cbz.universityforumsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cbz.universityforumsystem.dto.ArticleDto;
import com.cbz.universityforumsystem.dto.HandleArticleDto;
import com.cbz.universityforumsystem.dto.PublishArticleDto;
import com.cbz.universityforumsystem.entity.Article;
import com.cbz.universityforumsystem.utils.Result;

import java.time.LocalDateTime;

public interface ArticleService extends IService<Article> {

    Result publish(PublishArticleDto publishArticleDto);

    Result getMyCollect( LocalDateTime minTime, Long pageSize);

    Result getHotArticles();

    Result getMyArticles(LocalDateTime minTime, Long pageSize);

    Result loadArticles(LocalDateTime minTime, Long pageSize,boolean follow);

    Article getArticleById(Long articleId);

    Result handleArticleCollect(HandleArticleDto handleArticleDto);

    Result editArticle(ArticleDto articleDto);

    Result handleStatus(HandleArticleDto handleArticleDto);

    Result handleLike(HandleArticleDto handleArticleDto);

    Result deleteArticleById(Long articleId);

    Result getArticleDetail(Long articleId);

    Result getArticleByUserId(Long userId, LocalDateTime minTime, Long pageSize);
}
