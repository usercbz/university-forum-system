package com.cbz.universityforumsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cbz.universityforumsystem.dto.*;
import com.cbz.universityforumsystem.entity.User;
import com.cbz.universityforumsystem.utils.Result;

public interface UserService extends IService<User> {
    Result register(RegisterDto registerDto);

    Result sendCode(CodeDto codeDto);

    Result login(LoginDto loginDto);

    Result retrieve(RetrieveDto retrieveDto);

    Result getFollowAndFans();

    Result getUserFollow(ScrollPageDto pageDto);

    Result getUserFans(ScrollPageDto pageDto);

    Result handleUserFollow(HandleFollowDto followDto);

    Result updateUser(User user);

    Result removeFans(Long fanId);

    Result getUserDetails(Long userId);

    Result changePassword(PasswordDto passwordDto);
}
