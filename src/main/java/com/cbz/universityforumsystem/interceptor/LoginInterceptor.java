package com.cbz.universityforumsystem.interceptor;

import com.cbz.universityforumsystem.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.cbz.universityforumsystem.constant.RedisConstant.USER_LOGIN_TOKEN;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取token
        String token = request.getHeader("Authentication");
        if (token == null) {
            response.setStatus(401);
            return false;
        }
        //校验登录
        String userId = (String) redisTemplate.opsForValue().get(USER_LOGIN_TOKEN + token);
        if (userId == null) {
            response.setStatus(401);
            return false;
        }
        //存入上下文
        UserContext.setUser(Long.valueOf(userId));
        //放行
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserContext.removeUser();
    }
}
