package com.cbz.universityforumsystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cbz.universityforumsystem.entity.CommentMessage;
import com.cbz.universityforumsystem.entity.FollowMessage;
import com.cbz.universityforumsystem.entity.LikeMessage;
import com.cbz.universityforumsystem.entity.SystemNotification;
import com.cbz.universityforumsystem.mapper.SystemNotificationMapper;
import com.cbz.universityforumsystem.utils.Result;
import com.cbz.universityforumsystem.utils.UserContext;
import com.cbz.universityforumsystem.vo.NewsFlagVO;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.cbz.universityforumsystem.constant.RedisConstant.USER_NEWS_FLAG;

@Service
public class MessageService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AsyncTaskService asyncTaskService;

    @Autowired
    private SystemNotificationMapper systemNotificationMapper;

    public Result getMessageFlag() {

        Long currentUser = UserContext.getUser();

        Map<Object, Object> flag = redisTemplate.opsForHash().entries(USER_NEWS_FLAG + currentUser);

        if (flag.isEmpty()) {
            return Result.success(false);
        }
        for (Object value : flag.values()) {
            if (value.equals(1)) {
                return Result.success(true);
            }
        }
        return Result.success(false);
    }

    public Result getNewFlag() {
        Long currentUser = UserContext.getUser();

        Map<Object, Object> flag = redisTemplate.opsForHash().entries(USER_NEWS_FLAG + currentUser);
        NewsFlagVO newsFlagVO = new NewsFlagVO();
        Object like = flag.get("like");
        Object comment = flag.get("comment");
        Object sys = flag.get("sys");
        Object message = flag.get("message");
        if (Objects.equals(like, 1)) {
            newsFlagVO.setLike(true);
        }
        if (Objects.equals(comment, 1)) {
            newsFlagVO.setComment(true);
        }
        if (Objects.equals(sys, 1)) {
            newsFlagVO.setSys(true);
        }
        if (Objects.equals(message, 1)) {
            newsFlagVO.setMessage(true);
        }
        return Result.success(newsFlagVO);
    }

    public Result loadLikeMessage(LocalDateTime minTime) {
        Long currentUser = UserContext.getUser();
        Query query = getQuery(minTime, currentUser);
        List<LikeMessage> likeMessages = mongoTemplate.find(query, LikeMessage.class);

        asyncTaskService.handleLoadMessageAfter(currentUser, "like");
        return Result.success(likeMessages);
    }

    @NotNull
    private static Query getQuery(LocalDateTime minTime, Long currentUser) {
        minTime = minTime == null ? LocalDateTime.now() : minTime;
        return Query.query(Criteria.where("authorId").is(currentUser)
                        .and("updateTime").lt(minTime))
                .with(Sort.by(Sort.Direction.DESC, "updateTime"))
                .limit(20);
    }

    public Result loadReplyMessage(LocalDateTime minTime) {
        Long currentUser = UserContext.getUser();
        Query query = getQuery(minTime, currentUser);
        List<CommentMessage> commentMessages = mongoTemplate.find(query, CommentMessage.class);
        asyncTaskService.handleLoadMessageAfter(currentUser, "comment");
        return Result.success(commentMessages);
    }

    public Result loadMyMessage(LocalDateTime minTime) {
        Long currentUser = UserContext.getUser();
        Query query = getQuery(minTime, currentUser);
        List<FollowMessage> followMessages = mongoTemplate.find(query, FollowMessage.class);
        asyncTaskService.handleLoadMessageAfter(currentUser, "message");
        return Result.success(followMessages);
    }


    public Result loadSysMessage(LocalDateTime minTime) {
        Long currentUser = UserContext.getUser();
        LambdaQueryWrapper<SystemNotification> wrapper = Wrappers.lambdaQuery(SystemNotification.class)
                .lt(SystemNotification::getPublishTime, minTime)
                .orderByDesc(SystemNotification::getPublishTime)
                .last("limit 20");
        List<SystemNotification> list = systemNotificationMapper.selectList(wrapper);
        asyncTaskService.handleLoadMessageAfter(currentUser, "sys");
        return Result.success(list);
    }

}
