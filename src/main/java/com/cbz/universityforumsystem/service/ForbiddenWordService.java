package com.cbz.universityforumsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cbz.universityforumsystem.entity.ForbiddenWord;

import java.util.List;

public interface ForbiddenWordService extends IService<ForbiddenWord> {
    List<String> getWords();
}
