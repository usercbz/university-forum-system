package com.cbz.universityforumsystem.service;

import com.cbz.universityforumsystem.entity.SearchHistory;
import com.cbz.universityforumsystem.exception.SystemErrorException;
import com.cbz.universityforumsystem.utils.Result;
import com.cbz.universityforumsystem.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchHistoryService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AsyncTaskService asyncTaskService;

    /**
     * 加载用户用户搜索历史
     *
     * @return
     */
    public Result loadSearchHistory() {
        Long currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new SystemErrorException();
        }
        Query query = Query.query(Criteria.where("userId").is(currentUser))
                .with(Sort.by(Sort.Direction.DESC, "createTime"));
        List<SearchHistory> histories = mongoTemplate.find(query, SearchHistory.class);
        List<String> list = histories.stream().map(SearchHistory::getContent).collect(Collectors.toList());

        asyncTaskService.handleHistoryNumber(histories);
        return Result.success(list);
    }

    /**
     * 保存用户搜索历史
     *
     * @param content
     * @return
     */
    public Result saveHistory(String content) {
        Long currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new SystemErrorException();
        }
        Query query = Query.query(Criteria.where("userId").is(currentUser).and("content").is(content));
        //查询搜索历史
        SearchHistory history = mongoTemplate.findOne(query, SearchHistory.class);
        if (history == null) {
            history = new SearchHistory();
            history.setContent(content);
            history.setUserId(currentUser);
        }
        history.setCreateTime(LocalDateTime.now());
        mongoTemplate.save(history);
        return Result.success();
    }

    /**
     * 清空用户搜索历史
     *
     * @return
     */
    public Result clearHistory() {
        Long currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new SystemErrorException();
        }
        Query query = Query.query(Criteria.where("userId").is(currentUser));
        mongoTemplate.remove(query, SearchHistory.class);

        return Result.success();
    }
}
