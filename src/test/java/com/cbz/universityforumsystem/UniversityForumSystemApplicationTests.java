package com.cbz.universityforumsystem;

import com.cbz.universityforumsystem.entity.Article;
import com.cbz.universityforumsystem.entity.SearchArticle;
import com.cbz.universityforumsystem.entity.SearchUser;
import com.cbz.universityforumsystem.entity.User;
import com.cbz.universityforumsystem.service.ArticleService;
import com.cbz.universityforumsystem.service.SearchService;
import com.cbz.universityforumsystem.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@SpringBootTest
@Slf4j
class UniversityForumSystemApplicationTests {

    @Autowired
    private SearchService searchService;


    @Autowired
    private ArticleService articleService;


    @Test
    void test01() {
        for (Article article : articleService.list()) {
            SearchArticle searchArticle = new SearchArticle();
            searchArticle.setId(String.valueOf(article.getId()));
            searchArticle.setTitle(article.getTitle());
            searchArticle.setContent(article.getContent());
            searchArticle.setAuthorId(String.valueOf(article.getAuthorId()));
            searchArticle.setPublishTime(article.getPublishTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            searchArticle.setCover(article.getCover());
            searchService.saveArticleDoc(searchArticle);
        }
    }


}
