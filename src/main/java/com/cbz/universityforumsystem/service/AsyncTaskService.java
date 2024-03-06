package com.cbz.universityforumsystem.service;

import com.cbz.universityforumsystem.dto.ArticleDto;
import com.cbz.universityforumsystem.dto.CodeDto;
import com.cbz.universityforumsystem.dto.PublishArticleDto;
import com.cbz.universityforumsystem.entity.*;
import com.cbz.universityforumsystem.exception.SystemErrorException;
import com.cbz.universityforumsystem.mapper.ArticleBehaviorMapper;
import com.cbz.universityforumsystem.mapper.ArticleMapper;
import com.cbz.universityforumsystem.mapper.UserDetailMapper;
import com.cbz.universityforumsystem.mapper.UserMapper;
import com.cbz.universityforumsystem.utils.AESUtil;
import com.cbz.universityforumsystem.utils.JsonObjectMapper;
import com.cbz.universityforumsystem.utils.MyStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.cbz.universityforumsystem.constant.MessageConstant.REGISTER_CODE_MAIL_TEMPLATE;
import static com.cbz.universityforumsystem.constant.RedisConstant.*;
import static com.cbz.universityforumsystem.constant.SystemConstant.KEY_NAME;

/**
 * 异步任务处理器
 */
@Service
public class AsyncTaskService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserDetailMapper userDetailMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleBehaviorMapper articleBehaviorMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private ForbiddenWordService forbiddenWordService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SearchService searchService;

    /**
     * 在数据库内创建用户
     *
     * @param nickname 昵称
     * @param password 密码
     * @param email    邮箱
     */
    @Async
    public void createUserToDataBase(String account, String nickname, String password, String email) {

        //盐
        Random random = new Random();
        String salt = String.valueOf(random.nextInt(10)) +
                random.nextInt(10);
        //加密
        password = AESUtil.encrypt(password, System.getenv(KEY_NAME) + salt);

        User user = User.builder()
                .account(account)
                .nickname(nickname)
                .password(password)
                .salt(salt)
                .email(email)
                .createTime(LocalDateTime.now())
                .build();
        //保存
        userMapper.insert(user);
        //用户详细信息
        Long userId = user.getId();
        UserDetail userDetail = new UserDetail();
        userDetail.setUserId(userId);
        userDetailMapper.insert(userDetail);

        //创建倒排索引
        SearchUser searchUser = new SearchUser();

        searchService.saveUserDoc(searchUser);
    }

    //发送验证码邮件
    @Async
    public void sendCodeMail(CodeDto codeDto, String code) {
        String email = codeDto.getEmail();
        if (codeDto.getSubject() == 1) {
            //注册
            mailService.sendTextMailMessage(email, "注册用户",
                    String.format(REGISTER_CODE_MAIL_TEMPLATE, code));
        } else {
            //找回密码
            mailService.sendTextMailMessage(email, "找回密码", code);
        }
    }

    @Async
    public void saveArticle(User currentUser, PublishArticleDto publishArticleDto) {
        Long currentUserId = currentUser.getId();
        LocalDateTime publishTime = LocalDateTime.now();
        Article article = new Article();
        //属性拷贝
        BeanUtils.copyProperties(publishArticleDto, article);
        //获取内容
        String content = publishArticleDto.getContent();
        String title = publishArticleDto.getTitle();
        String cover = publishArticleDto.getCover();

        //内容脱敏 敏感词 -> ***
        List<String> words = forbiddenWordService.getWords();
        content = MyStringUtils.contentMask(content, words);
        title = MyStringUtils.contentMask(title, words);
        article.setContent(content);
        article.setTitle(title);
        article.setAuthorId(currentUserId);
        article.setPublishTime(publishTime);
        //保存
        articleMapper.insert(article);
        //用户文章加一
        redisTemplate.opsForHash().increment(USER_DETAIL + currentUserId, "articleNumber", 1);
        Long articleId = article.getId();
        //清空指定文章ID缓存
        redisTemplate.delete(ARTICLE_CACHE + articleId);

        //用户行为表
        ArticleBehavior articleBehavior = new ArticleBehavior();
        articleBehavior.setArticleId(articleId);
        articleBehaviorMapper.insert(articleBehavior);

        //创建搜索文档
        SearchArticle searchArticle = new SearchArticle();
        searchArticle.setId(String.valueOf(articleId));
        searchArticle.setTitle(title);
        searchArticle.setContent(content);
        searchArticle.setPublishTime(publishTime.format(DateTimeFormatter
                .ofPattern(JsonObjectMapper.DEFAULT_DATE_FORMAT)));
        searchArticle.setCover(cover);
        searchArticle.setAuthorId(String.valueOf(currentUserId));

        searchService.saveArticleDoc(searchArticle);

    }

    @Async
    public void handleArticleCacheAndNumber(Long userId, Long articleId, short option) {
        //修改用户收藏缓存、文章收藏数
        if (option == 1) {
            redisTemplate.opsForSet().add(USER_COLLECT + articleId, userId);
            redisTemplate.opsForHash()
                    .increment(ARTICLE_BEHAVIORAL_DATA + articleId, "collectionNumber", 1);
        } else {
            redisTemplate.opsForSet().remove(USER_COLLECT + articleId, userId);
            redisTemplate.opsForHash()
                    .increment(ARTICLE_BEHAVIORAL_DATA + articleId, "collectionNumber", -1);
        }
    }

    @Async
    public void handleHistoryNumber(List<SearchHistory> histories) {

        int size;
        if (histories == null || (size = histories.size()) < 20) {
            return;
        }
        SearchHistory history = histories.get(size - 1);
        mongoTemplate.remove(history);
    }

    @Async
    public void updateArticleDoc(ArticleDto articleDto) {
        //查询
        SearchArticle searchArticle = searchService.queryDoc(
                "article",
                articleDto.getId(),
                SearchArticle.class);
        String cover = articleDto.getCover();
        String title = articleDto.getTitle();
        String content = articleDto.getContent();
        List<String> words = forbiddenWordService.getWords();
        content = MyStringUtils.contentMask(content, words);
        title = MyStringUtils.contentMask(title, words);
        searchArticle.setTitle(title);
        searchArticle.setContent(content);
        searchArticle.setCover(cover);
        //保存
        searchService.saveArticleDoc(searchArticle);
    }

    @Async
    public void handleRemoveArticleAfter(Long articleId, Long authorId) {
        //清空缓存
        ArrayList<String> keys = new ArrayList<>();
        Collections.addAll(keys,
                ARTICLE_CACHE + articleId,
                ARTICLE_CACHE + articleId,
                USER_COLLECT + articleId);
        redisTemplate.delete(keys);
        //文章数减一
        redisTemplate.opsForHash().increment(USER_DETAIL + authorId, "articleNumber", -1);

        //删除文档记录
        searchService.deleteDoc("article", articleId);
    }

    @Async
    public void handleUpdateUserAfter(User user) {
        Long userId = user.getId();
        SearchUser searchUser = searchService.queryDoc("user", userId, SearchUser.class);
        searchUser.setNickname(user.getNickname());
        searchUser.setAvatar(user.getAvatar());
        searchService.saveUserDoc(searchUser);
    }

    @Async
    public void handleLoadMessageAfter(Long currentUser, String option) {
        redisTemplate.opsForHash().put(USER_NEWS_FLAG + currentUser, option, 0);
    }

    @Async
    public void handleUserFollowAfter(Long followUserId, Long userId, short option) {
        redisTemplate.opsForHash().increment(USER_DETAIL + followUserId, "fanNumber", option == 1 ? 1 : -1);
        redisTemplate.opsForHash().increment(USER_DETAIL + userId, "followNumber", option == 1 ? 1 : -1);
        //消息推送
        if (option == 1) {
            putFollowMessage(followUserId, userId);
        }

    }

    private void putLikeMessage(Long authorId, Object entityId, String content, short type, Long userId) {

        Query query = Query.query(Criteria.where("authorId").is(authorId)
                .and("entityId").is(entityId));
        LikeMessage likeMessage = mongoTemplate.findOne(query, LikeMessage.class);

        boolean isNull = false;

        if (likeMessage == null) {
            likeMessage = new LikeMessage();
            likeMessage.setLikeNumber(1);
            likeMessage.setAuthorId(authorId);
            likeMessage.setEntityId(entityId);
            likeMessage.setType(type);
            isNull = true;

        }
        if (!isNull) {
            long likeNumber = likeMessage.getLikeNumber();
            likeMessage.setLikeNumber(likeNumber + 1);
        }
        likeMessage.setUserId(userId);
        User user = userMapper.selectById(userId);
        likeMessage.setNickname(user.getNickname());
        likeMessage.setEntityContent(content);
        likeMessage.setUpdateTime(LocalDateTime.now());

        mongoTemplate.save(likeMessage);
        //修改标记
        redisTemplate.opsForHash().put(USER_NEWS_FLAG + authorId, "like", 1);
    }

    private void putReplyMessage(Long authorId, Object entityId, String entityContent, short type, User user, String commentContent) {
        CommentMessage commentMessage = new CommentMessage();
        commentMessage.setUpdateTime(LocalDateTime.now());
        commentMessage.setAuthorId(authorId);
        commentMessage.setEntityId(entityId);
        commentMessage.setEntityContent(entityContent);
        commentMessage.setType(type);
        commentMessage.setUserId(user.getId());
        commentMessage.setNickname(user.getNickname());
        commentMessage.setCommentContent(commentContent);

        mongoTemplate.save(commentMessage);
        //修改标记
        redisTemplate.opsForHash().put(USER_NEWS_FLAG + authorId, "comment", 1);
    }

    private void putFollowMessage(Long authorId, Long fanId) {
        FollowMessage followMessage = new FollowMessage();
        User fan = userMapper.selectById(fanId);
        followMessage.setAuthorId(authorId);
        followMessage.setFanId(fanId);
        followMessage.setFanNickname(fan.getNickname());
        followMessage.setFanAvatar(fan.getAvatar());
        followMessage.setUpdateTime(LocalDateTime.now());
        //保存消息
        mongoTemplate.save(followMessage);
        //修改标记
        redisTemplate.opsForHash().put(USER_NEWS_FLAG + authorId, "message", 1);
    }

    @Async
    public void handleArticleLikeAfter(Article article, Long currentUser, short option) {
        Long articleId = article.getId();
        if (option == 1) {
            //点赞
            redisTemplate.opsForSet().add(USER_LIKE + articleId, currentUser);
            //保存消息
            putLikeMessage(article.getAuthorId(), articleId, article.getContent(), (short) 0, currentUser);

        } else {
            //取消
            redisTemplate.opsForSet().remove(USER_LIKE + articleId, currentUser);
        }
        redisTemplate.opsForHash().increment(ARTICLE_BEHAVIORAL_DATA + articleId, "likeNumber", option == 1 ? 1 : -1);
    }

    @Async
    public void handleCommentLikeAfter(Comment comment, Long userId) {
        putLikeMessage(comment.getUserId(),
                comment.getId(),
                comment.getContent(),
                (short) 1,
                userId);
    }

    @Async
    public void handleReplyLikeAfter(CommentReply commentReply, Long userId) {
        putLikeMessage(commentReply.getTargetId(),
                commentReply.getId(),
                commentReply.getContent(),
                (short) 1,
                userId);
    }

    @Async
    public void publishCommentAfter(Long articleId, User user, String content) {
        //处理 文章评论数
        redisTemplate.opsForHash().increment(ARTICLE_BEHAVIORAL_DATA + articleId, "commentNumber", 1);
        Article article = articleMapper.selectById(articleId);
        putReplyMessage(article.getAuthorId(), articleId, article.getContent(), (short) 0, user, content);

    }

    @Async
    public void publishReplyAfter(CommentReply commentReply, User user) {

        String commentId = commentReply.getCommentId();
        Comment comment = mongoTemplate.findById(commentId, Comment.class);
        if (comment == null) {
            return;
        }
        comment.setReplyNumber(comment.getReplyNumber() + 1);
        mongoTemplate.save(comment);
        //文章评论数
        redisTemplate.opsForHash().increment(ARTICLE_BEHAVIORAL_DATA + comment.getArticleId(), "commentNumber", 1);

        //保存消息
        putReplyMessage(commentReply.getTargetId(),commentId,comment.getContent(),(short) 1,user,commentReply.getContent());

    }
}
