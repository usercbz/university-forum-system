package com.cbz.universityforumsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cbz.universityforumsystem.entity.User;
import com.cbz.universityforumsystem.mapper.UserMapper;
import com.cbz.universityforumsystem.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl  extends ServiceImpl<UserMapper,User> implements UserService {
}
