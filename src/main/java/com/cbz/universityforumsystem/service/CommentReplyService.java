package com.cbz.universityforumsystem.service;

import com.cbz.universityforumsystem.dto.ReplyLikeDto;
import com.cbz.universityforumsystem.dto.ReplySaveDto;
import com.cbz.universityforumsystem.entity.*;
import com.cbz.universityforumsystem.exception.SystemErrorException;
import com.cbz.universityforumsystem.utils.Result;
import com.cbz.universityforumsystem.utils.UserContext;
import com.cbz.universityforumsystem.vo.CommentReplyVO;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentReplyService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AsyncTaskService asyncTaskService;

    @Autowired
    private UserService userService;

    public Result loadReplies(String commentId) {
        Query query = Query.query(Criteria.where("commentId").is(commentId)).with(Sort.by(Sort.Direction.DESC, "publishTime"));
        List<CommentReply> commentReplies = mongoTemplate.find(query, CommentReply.class);

        if (commentReplies.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        Long userId = UserContext.getUser();
        //查询我点赞的回复
        List<String> replyIds = commentReplies.stream()
                .map(CommentReply::getId)
                .collect(Collectors.toList());
        Query query1 = Query.query(Criteria.where("replyId").in(replyIds).and("userId").is(userId));
        List<ReplyLike> replyLikes = mongoTemplate.find(query1, ReplyLike.class);
        List<String> replyLikeIds = replyLikes.stream()
                .map(ReplyLike::getReplyId)
                .collect(Collectors.toList());

        ArrayList<CommentReplyVO> commentReplyVOS = new ArrayList<>();
        for (CommentReply commentReply : commentReplies) {
            CommentReplyVO commentReplyVO = new CommentReplyVO();
            BeanUtils.copyProperties(commentReply, commentReplyVO);

            if (replyLikeIds.contains(commentReply.getId())) {
                commentReplyVO.setLiked(true);
            }
            commentReplyVOS.add(commentReplyVO);
        }

        return Result.success(commentReplyVOS);
    }

    public Result saveReply(ReplySaveDto replySaveDto) {

        String content = replySaveDto.getContent();
        if (content.length() > 140) {
            return Result.error("评论字数超出限制");
        }
        //TODO 敏感词判断

        Long currentId = UserContext.getUser();
        User user;
        if (currentId == null || (user = userService.getById(currentId)) == null) {
            throw new SystemErrorException();
        }
        CommentReply commentReply = createCommentReply(replySaveDto, content, user);
        //保存
        mongoTemplate.save(commentReply);
        CommentReplyVO commentReplyVO = new CommentReplyVO();
        BeanUtils.copyProperties(commentReply,commentReplyVO);
        //异步处理
        asyncTaskService.publishReplyAfter(commentReply,user);
        return Result.success(commentReplyVO);
    }

    @NotNull
    private CommentReply createCommentReply(ReplySaveDto replySaveDto, String content, User user) {
        CommentReply commentReply = new CommentReply();
        BeanUtils.copyProperties(replySaveDto, commentReply);
        commentReply.setUserId(user.getId());
        commentReply.setUserAvatar(user.getAvatar());
        commentReply.setNickname(user.getNickname());
        commentReply.setContent(content);
        commentReply.setLikeNumber(0L);
        commentReply.setPublishTime(LocalDateTime.now());
        return commentReply;
    }

    public Result handleLike(ReplyLikeDto replyLikeDto) {
        String replyId = replyLikeDto.getReplyId();
        short option = replyLikeDto.getOption();

        CommentReply commentReply = mongoTemplate.findById(replyId, CommentReply.class);

        if (commentReply == null) {
            throw new SystemErrorException();
        }
        Long userId = UserContext.getUser();
        long likeNumber = commentReply.getLikeNumber();
        if (option == 1) {
            //点赞
            commentReply.setLikeNumber(likeNumber + 1);
            mongoTemplate.save(commentReply);
            //保存点赞信息
            ReplyLike replyLike = new ReplyLike();
            replyLike.setReplyId(replyId);
            replyLike.setUserId(userId);
            mongoTemplate.save(replyLike);

            asyncTaskService.handleReplyLikeAfter(commentReply,userId);
        } else {
            //取消点赞
            long tmp = likeNumber - 1;
            tmp = tmp < 1 ? 0 : tmp;
            commentReply.setLikeNumber(tmp);
            mongoTemplate.save(commentReply);
            //删除点赞信息
            Query query = Query.query(Criteria.where("replyId").is(replyId).and("userId").is(userId));
            mongoTemplate.remove(query, ReplyLike.class);
        }
        return Result.success();
    }
}
