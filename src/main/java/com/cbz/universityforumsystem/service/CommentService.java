package com.cbz.universityforumsystem.service;

import com.cbz.universityforumsystem.dto.CommentDto;
import com.cbz.universityforumsystem.dto.CommentLikeDto;
import com.cbz.universityforumsystem.dto.CommentSaveDto;
import com.cbz.universityforumsystem.entity.Comment;
import com.cbz.universityforumsystem.entity.CommentLike;
import com.cbz.universityforumsystem.entity.User;
import com.cbz.universityforumsystem.exception.SystemErrorException;
import com.cbz.universityforumsystem.exception.UnauthorizedException;
import com.cbz.universityforumsystem.utils.Result;
import com.cbz.universityforumsystem.utils.UserContext;
import com.cbz.universityforumsystem.vo.CommentVO;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.cbz.universityforumsystem.constant.RedisConstant.ARTICLE_BEHAVIORAL_DATA;

@Service
public class CommentService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private AsyncTaskService asyncTaskService;

    public Result getComments(CommentDto commentDto) {

        Long articleId = commentDto.getArticleId();
        Integer pageSize = commentDto.getPageSize();
        LocalDateTime minTime = commentDto.getMinTime();

        pageSize = pageSize == null ? 20 : pageSize;
        minTime = minTime == null ? LocalDateTime.now() : minTime;

        Query query = Query.query(Criteria.where("articleId").is(articleId).and("publishTime").lt(minTime));
        query.with(Sort.by(Sort.Direction.DESC, "publishTime")).limit(pageSize);

        List<Comment> comments = mongoTemplate.find(query, Comment.class);

        if (comments.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        List<String> idList = comments.stream().map(Comment::getId).collect(Collectors.toList());
        Long currentId = UserContext.getUser();
        Query query1 = Query.query(Criteria.where("commentId").in(idList).and("userId").is(currentId));
        List<CommentLike> commentLikes = mongoTemplate.find(query1, CommentLike.class);
        List<String> likes = commentLikes.stream().map(CommentLike::getCommentId).collect(Collectors.toList());

        ArrayList<CommentVO> commentVOS = new ArrayList<>();
        for (Comment comment : comments) {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(comment, commentVO);
            if (likes.contains(comment.getId())) {
                commentVO.setLiked(true);
            }
            commentVOS.add(commentVO);
        }
        return Result.success(commentVOS);
    }

    public Result handleLike(CommentLikeDto commentLikeDto) {

        String commentId = commentLikeDto.getCommentId();

        short option = commentLikeDto.getOption();

        Comment comment = mongoTemplate.findById(commentId, Comment.class);
        if (comment == null) {
            throw new SystemErrorException();
        }
        Long userId = UserContext.getUser();
        if (option == 1) {
            //点赞
            comment.setLikeNumber(comment.getLikeNumber() + 1);
            mongoTemplate.save(comment);

            //保存点赞信息
            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(commentId);
            commentLike.setUserId(userId);

            mongoTemplate.save(commentLike);

            asyncTaskService.handleCommentLikeAfter(comment, userId);

        } else {
            //取消点赞
            long tmp = comment.getLikeNumber() - 1;
            tmp = tmp < 1 ? 0 : tmp;
            comment.setLikeNumber(tmp);
            mongoTemplate.save(comment);

            //删除点赞信息
            Query query = Query.query(Criteria.where("commentId").is(commentId).and("userId").is(userId));
            mongoTemplate.remove(query, CommentLike.class);
        }

        return Result.success();
    }

    public Result saveComment(CommentSaveDto commentSaveDto) {

        String content = commentSaveDto.getContent();
        Long articleId = commentSaveDto.getArticleId();

        //判断
        if (content.length() > 140) {
            return Result.error("评论内容不能超过140字");
        }
        // TODO 敏感词判断
        Long currentUserId = UserContext.getUser();

        User user;
        if (currentUserId == null || (user = userService.getById(currentUserId)) == null) {
            throw new UnauthorizedException();
        }
        Comment comment = createComment(content, articleId, user);
        mongoTemplate.save(comment);
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);

        asyncTaskService.publishCommentAfter(articleId,user,content);

        return Result.success(commentVO);
    }

    @NotNull
    private Comment createComment(String content, Long articleId, User user) {
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(user.getId());
        comment.setNickname(user.getNickname());
        comment.setUserAvatar(user.getAvatar());
        comment.setContent(content);
        comment.setLikeNumber(0L);
        comment.setReplyNumber(0L);
        comment.setPublishTime(LocalDateTime.now());
        return comment;
    }
}
