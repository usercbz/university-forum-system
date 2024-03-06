package com.cbz.universityforumsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cbz.universityforumsystem.config.MinIOConfigProperties;
import com.cbz.universityforumsystem.dto.ArticleDto;
import com.cbz.universityforumsystem.dto.HandleArticleDto;
import com.cbz.universityforumsystem.dto.PublishArticleDto;
import com.cbz.universityforumsystem.entity.Article;
import com.cbz.universityforumsystem.entity.User;
import com.cbz.universityforumsystem.entity.UserCollect;
import com.cbz.universityforumsystem.entity.UserFollow;
import com.cbz.universityforumsystem.exception.ChangeFailedException;
import com.cbz.universityforumsystem.exception.NoPermissionException;
import com.cbz.universityforumsystem.exception.SystemErrorException;
import com.cbz.universityforumsystem.mapper.ArticleMapper;
import com.cbz.universityforumsystem.mapper.UserCollectMapper;
import com.cbz.universityforumsystem.mapper.UserFollowMapper;
import com.cbz.universityforumsystem.service.ArticleService;
import com.cbz.universityforumsystem.service.AsyncTaskService;
import com.cbz.universityforumsystem.service.UserService;
import com.cbz.universityforumsystem.utils.Result;
import com.cbz.universityforumsystem.utils.UserContext;
import com.cbz.universityforumsystem.vo.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.cbz.universityforumsystem.constant.RedisConstant.*;
import static com.cbz.universityforumsystem.constant.SystemConstant.NO_IMAGE;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserCollectMapper userCollectMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private AsyncTaskService asyncTaskService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MinIOConfigProperties minIOConfigProperties;

    @Override
    public Result publish(PublishArticleDto publishArticleDto) {

        Long currentUserId = UserContext.getUser();
        User currentUser;
        if (currentUserId == null || (currentUser = userService.getById(currentUserId)) == null) {
            throw new SystemErrorException();
        }
        //保存文章，异步保存
        asyncTaskService.saveArticle(currentUser, publishArticleDto);

        return Result.success();
    }

    @Override
    public Result getMyCollect(LocalDateTime minTime, Long pageSize) {

        Long currentUserId = UserContext.getUser();
        minTime = minTime == null ? LocalDateTime.now() : minTime;

        LambdaQueryWrapper<UserCollect> queryWrapper = Wrappers.lambdaQuery(UserCollect.class)
                .eq(UserCollect::getUserId, currentUserId)
                .lt(UserCollect::getCreateTime, minTime)
                .orderByDesc(UserCollect::getCreateTime)
                .last("limit " + pageSize);

        List<UserCollect> userCollects = userCollectMapper.selectList(queryWrapper);
        if (userCollects.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<Long> articleIdList = userCollects.stream()
                .map(UserCollect::getArticleId)
                .collect(Collectors.toList());
        String articleIds = StringUtils.join(articleIdList, ",");
        //查询文章
        LambdaQueryWrapper<Article> articleQueryWrapper = Wrappers.lambdaQuery(Article.class)
                .in(Article::getId, articleIdList)
                .last("order by field ( id ," + articleIds + ")");

        List<Article> articleList = list(articleQueryWrapper);
        List<MyCollectVO> myCollectVOS = getMyCollectVOS(userCollects, articleList);

        return Result.success(myCollectVOS);
    }

    @NotNull
    private List<MyCollectVO> getMyCollectVOS(List<UserCollect> userCollects, List<Article> articleList) {
        List<MyCollectVO> myCollectVOS = new ArrayList<>();

        for (int i = 0; i < userCollects.size(); i++) {
            MyCollectVO myCollectVO = new MyCollectVO();
            Article article = articleList.get(i);
            BeanUtils.copyProperties(article, myCollectVO);
            Long authorId = article.getAuthorId();
            User author = userService.getById(authorId);
            myCollectVO.setAuthorName(author.getNickname());
            myCollectVO.setCreateTime(userCollects.get(i).getCreateTime());
            if (article.getCover() == null) {
                myCollectVO.setCover(minIOConfigProperties.getReadPath() + NO_IMAGE);
            }
            myCollectVOS.add(myCollectVO);
        }
        return myCollectVOS;
    }

    @Override
    public Result getHotArticles() {
        String jsonStr = (String) redisTemplate.opsForValue().get(HOT_ARTICLE);
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        List<HotArticleVO> hotArticleVOS;
        if (jsonStr == null) {
            throw new SystemErrorException("热点数据未导入");
        }
        try {
            hotArticleVOS = jsonObjectMapper.readValue(jsonStr,
                    new TypeReference<List<HotArticleVO>>() {
                    });
        } catch (JsonProcessingException e) {
            log.error("json解析异常", e);
            throw new SystemErrorException();
        }
        return Result.success(hotArticleVOS);
    }

    @Override
    public Result getMyArticles(LocalDateTime minTime, Long pageSize) {
        Long currentUserId = UserContext.getUser();
        minTime = minTime == null ? LocalDateTime.now() : minTime;
        //查询我的文章
        LambdaQueryWrapper<Article> queryWrapper = Wrappers.lambdaQuery(Article.class)
                .eq(Article::getAuthorId, currentUserId)
                .lt(Article::getPublishTime, minTime)
                .orderByDesc(Article::getPublishTime)
                .last("limit " + pageSize);//分页

        List<Article> articles = list(queryWrapper);
        return Result.success(articles);
    }

    @Override
    public Result loadArticles(LocalDateTime minTime, Long pageSize, boolean follow) {

        Long currentUserId = UserContext.getUser();

        minTime = minTime == null ? LocalDateTime.now() : minTime;

        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();

        if (follow) {
            //查询用户关注
            List<UserFollow> userFollows = userFollowMapper
                    .selectList(Wrappers.lambdaQuery(UserFollow.class)
                            .eq(UserFollow::getUserId, currentUserId));

            if (userFollows.isEmpty()) {
                return Result.success(Collections.emptyList());
            }
            List<Long> followIds = userFollows.stream()
                    .map(UserFollow::getFollowId)
                    .collect(Collectors.toList());

            queryWrapper.in(Article::getAuthorId, followIds);
            queryWrapper.ne(Article::getVisible, 2);
        } else {
            queryWrapper.eq(Article::getVisible, 0);
        }

        queryWrapper.lt(Article::getPublishTime, minTime)
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getPublishTime)
                .last("limit " + pageSize);
        //加载文章数据
        List<Article> list = list(queryWrapper);
        if (list.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        ArrayList<ArticleVO> articleVOS = new ArrayList<>();
        for (Article article : list) {
            ArticleVO articleVO = getArticleVO(article, currentUserId);
            articleVOS.add(articleVO);
        }
        return Result.success(articleVOS);
    }

    @NotNull
    private ArticleVO getArticleVO(Article article, Long currentUserId) {
        Long articleId = article.getId();
        //获取文章行为数据
        Map<Object, Object> entries = redisTemplate.opsForHash()
                .entries(ARTICLE_BEHAVIORAL_DATA + articleId);
        //likeNumber、collectionNumber、commentNumber
        Object likeNumber = entries.get("likeNumber");
        Object collectionNumber = entries.get("collectionNumber");
        Object commentNumber = entries.get("commentNumber");

        likeNumber = likeNumber == null ? 0 : likeNumber;
        collectionNumber = collectionNumber == null ? 0 : collectionNumber;
        commentNumber = commentNumber == null ? 0 : commentNumber;

        //是否点赞
        Boolean isLiked = redisTemplate.opsForSet().isMember(USER_LIKE + articleId, currentUserId);
        //是否收藏
        Boolean isCollect = redisTemplate.opsForSet().isMember(USER_COLLECT + articleId, currentUserId);

        ArticleVO articleVO = new ArticleVO();
        //属性拷贝
        BeanUtils.copyProperties(article, articleVO);
        Long authorId = article.getAuthorId();
        User author = userService.getById(authorId);
        articleVO.setAuthorName(author.getNickname());
        articleVO.setLikeNumber(likeNumber);
        articleVO.setCollectionNumber(collectionNumber);
        articleVO.setCommentNumber(commentNumber);

        articleVO.setLiked(Boolean.TRUE.equals(isLiked));
        articleVO.setCollected(Boolean.TRUE.equals(isCollect));
        return articleVO;
    }

    @Override
    public Article getArticleById(Long articleId) {
        //在缓存查找
        Object obj = redisTemplate.opsForValue().get(ARTICLE_CACHE + articleId);
        Article article = null;
        if (obj instanceof Article) {
            //命中
            article = (Article) obj;
        } else if (!NULL_VALUE.equals(obj)) {
            //未命中
            article = getById(articleId);
            redisTemplate.opsForValue().set(ARTICLE_CACHE + articleId,
                    article == null ? NULL_VALUE : article,//解决缓存穿透
                    3L, TimeUnit.HOURS);
        }
        return article;
    }

    @Override
    public Result handleArticleCollect(HandleArticleDto handleArticleDto) {

        Long currentUser = UserContext.getUser();
        Long articleId = handleArticleDto.getArticleId();
        short option = handleArticleDto.getOption();

        Article article = getArticleById(articleId);
        if (article != null) {
            try {
                if (option == 1) {
                    UserCollect userCollect = new UserCollect();
                    userCollect.setArticleId(articleId);
                    userCollect.setUserId(currentUser);
                    userCollect.setCreateTime(LocalDateTime.now());

                    if (userCollectMapper.insert(userCollect) == 0) {
                        throw new SystemErrorException();
                    }
                } else {
                    //取消收藏
                    if (userCollectMapper.delete(Wrappers.lambdaQuery(UserCollect.class)
                            .eq(UserCollect::getUserId, currentUser).eq(UserCollect::getArticleId, articleId)) == 0) {
                        throw new SystemErrorException();
                    }
                }
            } catch (Exception e) {
                throw new SystemErrorException();
            }
        } else {
            throw new SystemErrorException();
        }

        //异步操作、处理文章收藏缓存、文章收藏数
        asyncTaskService.handleArticleCacheAndNumber(currentUser, articleId, option);

        return Result.success();
    }

    @Override
    public Result editArticle(ArticleDto articleDto) {

        Long currentUser = UserContext.getUser();
        Long id = articleDto.getId();
        Article article = getArticleById(id);

        if (article != null) {
            //判断当前用户
            if (currentUser.equals(article.getAuthorId())) {
                //修改
                Article updateArticle = new Article();
                BeanUtils.copyProperties(articleDto, updateArticle);
                if (updateById(updateArticle)) {
                    //异步处理
                    asyncTaskService.updateArticleDoc(articleDto);
                    return Result.success();
                } else {
                    throw new ChangeFailedException();
                }
            } else {
                throw new NoPermissionException();
            }

        } else {
            throw new SystemErrorException();
        }
    }

    @Override
    public Result handleStatus(HandleArticleDto handleArticleDto) {
        Long articleId = handleArticleDto.getArticleId();
        short option = handleArticleDto.getOption();

        if (getArticleById(articleId) != null &&
                update(Wrappers.lambdaUpdate(Article.class)
                        .eq(Article::getId, articleId)
                        .set(Article::getStatus, option == 1 ? 1 : 0))) {
            //清空Redis里的文章信息
            redisTemplate.delete(ARTICLE_CACHE + articleId);
            return Result.success();
        }
        throw new SystemErrorException();

    }

    @Override
    public Result handleLike(HandleArticleDto handleArticleDto) {

        Long currentUser = UserContext.getUser();

        Long articleId = handleArticleDto.getArticleId();
        short option = handleArticleDto.getOption();

        Article article = getArticleById(articleId);
        if (article != null) {
            //处理文章点赞
            asyncTaskService.handleArticleLikeAfter(article,currentUser,option);

        } else {
            throw new SystemErrorException();
        }

        return Result.success();
    }

    @Override
    public Result deleteArticleById(Long articleId) {
        //判断
        Long currentUserId = UserContext.getUser();

        User currentUser;
        if (currentUserId == null || (currentUser = userService.getById(currentUserId)) == null) {
            throw new SystemErrorException();
        }
        Article article = getById(articleId);
        Long authorId = article.getAuthorId();
        if (currentUser.getPermission() == 0 && !currentUserId.equals(authorId)) {
            throw new NoPermissionException();
        }
        //删除
        if (removeById(articleId)) {
            asyncTaskService.handleRemoveArticleAfter(articleId,authorId);
        } else {
            throw new ChangeFailedException();
        }
        return Result.success();
    }

    @Override
    public Result getArticleDetail(Long articleId) {
        //查询文章
        Article article = getArticleById(articleId);
        Long currentUserId = UserContext.getUser();
        User currentUser;
        if (article == null || currentUserId == null || (currentUser = userService.getById(currentUserId)) == null) {
            throw new SystemErrorException();
        }
        if ((article.getStatus() == 0 || article.getVisible() == 2) &&
                (currentUser.getPermission() == 1 || !currentUserId.equals(article.getAuthorId()))) {
            throw new NoPermissionException();
        }
        ArticleDetailVO articleDetailVO = new ArticleDetailVO();
        BeanUtils.copyProperties(article, articleDetailVO);
        Long authorId = article.getAuthorId();
        User author;
        if (currentUserId.equals(authorId)) {
            author = currentUser;
        } else {
            author = userService.getById(authorId);
        }
        if (author == null) {
            throw new SystemErrorException();
        }
        articleDetailVO.setAuthorName(author.getNickname());
        articleDetailVO.setAuthorAvatar(author.getAvatar());
        articleDetailVO.setDescription(author.getDescription());
        articleDetailVO.setAuthorSex(author.getSex());
        short relation = 2;
        if (currentUserId.equals(authorId)) {
            //作者是自己
            relation = 0;
        } else if (userFollowMapper.selectOne(Wrappers.lambdaQuery(UserFollow.class)
                .eq(UserFollow::getFollowId, authorId)
                .eq(UserFollow::getUserId, currentUserId)) != null) {
            //是粉丝
            relation = 1;
        }
        articleDetailVO.setRelation(relation);
        //查询评论数
        Map<Object, Object> entries = redisTemplate.opsForHash()
                .entries(ARTICLE_BEHAVIORAL_DATA + articleId);
        Object commentNumber = entries.get("commentNumber");
        commentNumber = commentNumber == null ? 0 : commentNumber;

        articleDetailVO.setCommentNumber(commentNumber);
        //判断文章是否点赞、收藏
        //是否点赞
        Boolean isLiked = redisTemplate.opsForSet().isMember(USER_LIKE + articleId, currentUserId);
        //是否收藏
        Boolean isCollect = redisTemplate.opsForSet().isMember(USER_COLLECT + articleId, currentUserId);
        articleDetailVO.setLiked(Boolean.TRUE.equals(isLiked));
        articleDetailVO.setCollected(Boolean.TRUE.equals(isCollect));

        return Result.success(articleDetailVO);
    }

    @Override
    public Result getArticleByUserId(Long userId, LocalDateTime minTime, Long pageSize) {
        Long currentUserId = UserContext.getUser();
        minTime = minTime == null ? LocalDateTime.now() : minTime;

        LambdaQueryWrapper<Article> queryWrapper = Wrappers.lambdaQuery(Article.class)
                .eq(Article::getAuthorId, userId)
                .eq(Article::getStatus, 1)
                .lt(Article::getPublishTime, minTime)
                .orderByDesc(Article::getPublishTime);

        if (currentUserId.equals(userId)) {
            //自己
            return getMyArticles(minTime, pageSize);
        } else if (userFollowMapper.selectOne(Wrappers.lambdaQuery(UserFollow.class).eq(UserFollow::getFollowId, userId).eq(UserFollow::getUserId, currentUserId)) != null) {
            //粉丝
            queryWrapper.ne(Article::getVisible, 2);
        } else {
            queryWrapper.eq(Article::getVisible, 0);
        }
        //查询文章
        List<Article> articles = list(queryWrapper);
        if (articles.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        ArrayList<UserArticleVO> userArticleVOS = new ArrayList<>();
        for (Article article : articles) {
            UserArticleVO userArticleVO = new UserArticleVO();
            BeanUtils.copyProperties(article, userArticleVO);
            Long articleId = article.getId();
            //是否点赞
            Boolean isLiked = redisTemplate.opsForSet().isMember(USER_LIKE + articleId, currentUserId);
            if (Boolean.TRUE.equals(isLiked)) {
                userArticleVO.setLiked(true);
            }
            Object likeNumber =  redisTemplate.opsForHash().get(ARTICLE_BEHAVIORAL_DATA + articleId, "likeNumber");
            likeNumber = likeNumber == null ? 0 : likeNumber;
            userArticleVO.setLikeNumber((Integer) likeNumber);
            userArticleVOS.add(userArticleVO);
        }
        return Result.success(userArticleVOS);
    }
}
