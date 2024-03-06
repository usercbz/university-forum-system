package com.cbz.universityforumsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cbz.universityforumsystem.entity.ForbiddenWord;
import com.cbz.universityforumsystem.mapper.ForbiddenWordMapper;
import com.cbz.universityforumsystem.service.ForbiddenWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.cbz.universityforumsystem.constant.RedisConstant.SENSITIVE_WORDS;

@Service
public class ForbiddenWordServiceImpl extends ServiceImpl<ForbiddenWordMapper, ForbiddenWord> implements ForbiddenWordService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<String> getWords() {
        Set<Object> words = redisTemplate.opsForSet().members(SENSITIVE_WORDS);

        if (words == null || words.isEmpty()) {
            return Collections.emptyList();
        }
        return words.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
