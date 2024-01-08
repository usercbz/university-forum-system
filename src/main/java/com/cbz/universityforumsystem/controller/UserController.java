package com.cbz.universityforumsystem.controller;

import com.cbz.universityforumsystem.entity.User;
import com.cbz.universityforumsystem.service.UserService;
import com.cbz.universityforumsystem.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("login")
    public Result login(@RequestBody User user){
        return null;
    }

    @RequestMapping("register")
    public Result register(@RequestBody User user){
        return null;
    }
}
