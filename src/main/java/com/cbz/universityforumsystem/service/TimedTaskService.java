package com.cbz.universityforumsystem.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cbz.universityforumsystem.entity.Article;
import com.cbz.universityforumsystem.entity.ArticleBehavior;
import com.cbz.universityforumsystem.entity.UserDetail;
import com.cbz.universityforumsystem.exception.SystemErrorException;
import com.cbz.universityforumsystem.mapper.ArticleBehaviorMapper;
import com.cbz.universityforumsystem.mapper.ArticleMapper;
import com.cbz.universityforumsystem.mapper.UserDetailMapper;
import com.cbz.universityforumsystem.vo.HotArticleVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.cbz.universityforumsystem.constant.RedisConstant.*;
import static com.cbz.universityforumsystem.constant.ScoreWeight.*;

/**
 * 定时任务调度
 */
@Service
public class TimedTaskService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserDetailMapper userDetailMapper;

    @Autowired
    private ArticleBehaviorMapper articleBehaviorMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 处理文章点赞等信息
     */
    @Scheduled(cron = "* * 0/1 * * ? *")//每小时执行一次
    public void handleArticleDetails() {
        Set<String> keys = getKeys(ARTICLE_BEHAVIORAL_DATA + "*");
        for (String key : keys) {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries.isEmpty()) {
                continue;
            }
            ArticleBehavior articleBehavior = getArticleBehavior(key, entries);
            //保存
            articleBehaviorMapper.updateById(articleBehavior);
        }

    }

    /**
     * 计算热点文章数据
     */
    @Scheduled(cron = "* 0/30 * * * ? *")
    public void computeHotArticle() {
        LocalDateTime now = LocalDateTime.now();
        List<Article> articles = articleMapper.selectList(Wrappers.lambdaQuery(Article.class)
                .gt(Article::getPublishTime, now.minusDays(5))
                .select(Article::getId));
        List<Long> articleIds = articles.stream()
                .map(Article::getId)
                .collect(Collectors.toList());

        PriorityQueue<Map.Entry<Long, Long>> priorityQueue =
                new PriorityQueue<>((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        for (Long articleId : articleIds) {
            Map<Object, Object> entries = redisTemplate.opsForHash()
                    .entries(ARTICLE_BEHAVIORAL_DATA + articleId);
            ArticleBehavior articleBehavior = handleMap(entries);
            long score = articleBehavior.getBrowseNumber() * BROWSE +
                    articleBehavior.getLikeNumber() * LIKE +
                    articleBehavior.getCommentNumber() * COMMENT +
                    articleBehavior.getCollectionNumber() * COLLECTION;
            priorityQueue.offer(new AbstractMap.SimpleEntry<>(articleId, score));
        }
        ArrayList<HotArticleVO> hotArticleVOS = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Map.Entry<Long, Long> entry = priorityQueue.poll();
            if (entry == null) {
                break;
            }
            Long articleId = entry.getKey();
            Article article = articles.stream()
                    .filter(e -> articleId.equals(e.getId()))
                    .findFirst().get();

            HotArticleVO hotArticleVO = new HotArticleVO();
            hotArticleVO.setArticleId(articleId);
            hotArticleVO.setTitle(article.getTitle());
            hotArticleVOS.add(hotArticleVO);
        }

        if (hotArticleVOS.isEmpty()) {
            return;
        }
        try {
            redisTemplate.opsForValue()
                    .set(HOT_ARTICLE, objectMapper.writeValueAsString(hotArticleVOS));
        } catch (JsonProcessingException e) {
            throw new SystemErrorException();
        }
    }

    @NotNull
    private static ArticleBehavior handleMap(Map<Object, Object> entries) {
        Object likeNumber = entries.get("likeNumber");
        likeNumber = likeNumber == null ? 0L : likeNumber;
        Object collectionNumber = entries.get("collectionNumber");
        collectionNumber = collectionNumber == null ? 0L : collectionNumber;
        Object browseNumber = entries.get("browseNumber");
        browseNumber = browseNumber == null ? 0L : browseNumber;
        Object commentNumber = entries.get("commentNumber ");
        commentNumber = commentNumber == null ? 0L : commentNumber;

        ArticleBehavior articleBehavior = new ArticleBehavior();

        articleBehavior.setLikeNumber((Integer) likeNumber);
        articleBehavior.setBrowseNumber((Integer) browseNumber);
        articleBehavior.setCommentNumber((Integer) commentNumber);
        articleBehavior.setCollectionNumber((Integer) collectionNumber);
        return articleBehavior;
    }

    /**
     * 保存用户关注，粉丝文章等信息
     */
    @Scheduled(cron = "* * 0/2 * * ? *")//两小时执行一次
    public void saveUserDetails() {
        for (String key : getKeys(USER_DETAIL + "*")) {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries.isEmpty()) {
                continue;
            }
            UserDetail userDetail = getUserDetail(key, entries);
            userDetailMapper.updateById(userDetail);
        }
    }

    @NotNull
    private ArticleBehavior getArticleBehavior(String key, Map<Object, Object> entries) {

        ArticleBehavior articleBehavior = handleMap(entries);
        String articleId = key.replace(ARTICLE_BEHAVIORAL_DATA, "");
        articleBehavior.setArticleId(Long.valueOf(articleId));

        return articleBehavior;
    }

    @NotNull
    private Set<String> getKeys(String pattern) {
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(pattern)
                .count(20)
                .build();
        Cursor<String> cursor = redisTemplate.scan(scanOptions);
        Set<String> keys = new HashSet<>();
        while (cursor.hasNext()) {
            keys.add(cursor.next());
        }
        return keys;
    }


    @NotNull
    private UserDetail getUserDetail(String key, Map<Object, Object> entries) {
        String userId = key.replace(USER_DETAIL, "");

        Object articleNumber = entries.get("articleNumber");
        Object followNumber = entries.get("followNumber ");
        Object fanNumber = entries.get("fanNumber");

        if (articleNumber == null) articleNumber = 0L;
        if (followNumber == null) followNumber = 0L;
        if (fanNumber == null) fanNumber = 0L;

        UserDetail userDetail = new UserDetail();
        userDetail.setUserId(Long.valueOf(userId));
        userDetail.setArticleNumber((Integer) articleNumber);
        userDetail.setFollowNumber((Integer) followNumber);
        userDetail.setFanNumber((Integer) fanNumber);
        return userDetail;
    }
}
