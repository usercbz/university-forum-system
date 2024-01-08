package com.cbz.universityforumsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cbz.universityforumsystem.entity.User;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    List<String> selectDuplicateAccount(List<String> accounts);
}
