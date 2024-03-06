package com.cbz.universityforumsystem.controller;

import com.cbz.universityforumsystem.dto.*;
import com.cbz.universityforumsystem.entity.User;
import com.cbz.universityforumsystem.service.UserService;
import com.cbz.universityforumsystem.utils.Result;
import com.cbz.universityforumsystem.utils.UserContext;
import com.cbz.universityforumsystem.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    //获取关注信息
    @GetMapping("follow/info")
    public Result getFollowInfo() {
        return userService.getFollowAndFans();
    }

    //获取用户关注信息
    @PostMapping("follow")
    public Result getUserFollow(@RequestBody ScrollPageDto pageDto) {
        return userService.getUserFollow(pageDto);
    }

    //获取粉丝信息
    @PostMapping("fans")
    public Result getUserFans(@RequestBody ScrollPageDto pageDto) {
        return userService.getUserFans(pageDto);
    }

    //修改个人信息
    @PutMapping
    public Result updatePersonalDetails(@RequestBody User user) {
        return userService.updateUser(user);
    }

    //获取个人信息
    @GetMapping
    public Result getUserInfo() {
        User user = userService.getById(UserContext.getUser());
        UserVO userVO = new UserVO();
        if (user != null) {
            BeanUtils.copyProperties(user, userVO);
        }
        return Result.success(userVO);
    }

    //用户关注、取关
    @PutMapping("/handle/follow")
    public Result handlerUserFollow(@Validated @RequestBody HandleFollowDto followDto) {
        return userService.handleUserFollow(followDto);
    }

    //移除粉丝
    @DeleteMapping("/remove/fans/{fanId}")
    public Result removeFans(@PathVariable Long fanId) {
        return userService.removeFans(fanId);
    }

    @PostMapping("login")
    public Result login(@Validated @RequestBody LoginDto loginDto) {
        log.info("登录信息：{}", loginDto);
        return userService.login(loginDto);
    }

    //用户注册
    @PostMapping("register")
    public Result register(@Validated @RequestBody RegisterDto registerDto) {
        log.info("{}", registerDto);
        return userService.register(registerDto);
    }

    //找回密码
    @PostMapping("retrieve")
    public Result retrieve(@Validated @RequestBody RetrieveDto retrieveDto) {
        return userService.retrieve(retrieveDto);
    }

    //发送验证码
    @PostMapping("code")
    public Result sendCode(@Validated @RequestBody CodeDto codeDto) {
        return userService.sendCode(codeDto);
    }

    @GetMapping("/details/{userId}")
    public Result getUserDetails(@PathVariable Long userId) {
        return userService.getUserDetails(userId);
    }

    @PutMapping("/pass")
    public Result changePassword(@Validated @RequestBody PasswordDto passwordDto){
        return userService.changePassword(passwordDto);
    }
}
