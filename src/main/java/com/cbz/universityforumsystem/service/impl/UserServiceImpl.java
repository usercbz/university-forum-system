package com.cbz.universityforumsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cbz.universityforumsystem.dto.*;
import com.cbz.universityforumsystem.entity.User;
import com.cbz.universityforumsystem.entity.UserDetail;
import com.cbz.universityforumsystem.entity.UserFollow;
import com.cbz.universityforumsystem.exception.BaseException;
import com.cbz.universityforumsystem.exception.ChangeFailedException;
import com.cbz.universityforumsystem.exception.NoPermissionException;
import com.cbz.universityforumsystem.exception.SystemErrorException;
import com.cbz.universityforumsystem.mapper.UserDetailMapper;
import com.cbz.universityforumsystem.mapper.UserFollowMapper;
import com.cbz.universityforumsystem.mapper.UserMapper;
import com.cbz.universityforumsystem.service.AsyncTaskService;
import com.cbz.universityforumsystem.service.UserService;
import com.cbz.universityforumsystem.utils.AESUtil;
import com.cbz.universityforumsystem.utils.AccountUtil;
import com.cbz.universityforumsystem.utils.Result;
import com.cbz.universityforumsystem.utils.UserContext;
import com.cbz.universityforumsystem.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.cbz.universityforumsystem.constant.RedisConstant.*;
import static com.cbz.universityforumsystem.constant.SystemConstant.KEY_NAME;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private AccountUtil accountUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AsyncTaskService asyncTaskService;

    @Autowired
    private UserDetailMapper userDetailMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Override
    public Result register(RegisterDto registerDto) {
        String checkCode = registerDto.getCheckCode();
        String email = registerDto.getEmail();
        //校验验证码
        Object code = redisTemplate.opsForValue().get(USER_MAIL_CODE + email);
        if (!checkCode.equals(code)) {
            //验证码不一致
            return Result.error("注册失败，验证码错误");
        }
        //成功，获取账号
        String account = accountUtil.get();
        //注册用户，异步处理
        asyncTaskService.createUserToDataBase(account, registerDto.getNickname(), registerDto.getPassword(), email);
        //返回账号
        return Result.success(account);
    }

    @Override
    public Result sendCode(CodeDto codeDto) {
        //生成验证码
        String code = createCode();
        String email = codeDto.getEmail();
        //存入Redis
        redisTemplate.opsForValue()
                .set(USER_MAIL_CODE + email, code, 10L, TimeUnit.MINUTES);
        //发送邮件 异步发送
        asyncTaskService.sendCodeMail(codeDto, code);
        //返回
        return Result.success();
    }

    @Override
    public Result login(LoginDto loginDto) {
        String account = loginDto.getAccount();
        String password = loginDto.getPassword();
        //根据账号查询用户信息
        User user = getOne(Wrappers.lambdaQuery(User.class).eq(User::getAccount, account));
        if (user == null) {
            return Result.error("用户不存在");
        }
        //user != null 校验 状态 、密码
        String dbPass = user.getPassword();
        boolean isValid = password.equals(AESUtil.decrypt(dbPass, System.getenv(KEY_NAME) + user.getSalt()));
        if (!isValid) {
            return Result.error("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            return Result.error("账号状态异常");
        }
        //密码正确，将信息存入token返回
        //生成token
        String token = UUID.randomUUID().toString();
        //存入Redis
        redisTemplate.opsForValue().set(USER_LOGIN_TOKEN + token, user.getId(), 1L, TimeUnit.DAYS);
        //返回token
        return Result.success(token);
    }

    @Override
    public Result retrieve(RetrieveDto retrieveDto) {
        String email = retrieveDto.getEmail();
        String account = retrieveDto.getAccount();
        String checkCode = retrieveDto.getCheckCode();
        String password = retrieveDto.getPassword();
        //判断账号和邮箱
        User user = getOne(Wrappers.lambdaQuery(User.class)
                .eq(User::getAccount, account));
        //判断账号
        if (user == null) {
            return Result.error("该用户不存在！");
        }
        //判断邮箱
        if (!email.equals(user.getEmail())) {
            return Result.error("账号或邮箱错误！");
        }
        //判断验证码
        Object code = redisTemplate.opsForValue().get(USER_MAIL_CODE + email);
        if (!checkCode.equals(code)) {
            return Result.error("验证码错误");
        }
        //重置密码
        String salt = user.getSalt();
        //加密
        password = AESUtil.encrypt(password, System.getenv(KEY_NAME) + salt);
        user.setPassword(password);
        save(user);
        return Result.success();
    }

    /**
     * 获取粉丝数和关注数
     *
     * @return 粉丝数、关注数
     */
    @Override
    public Result getFollowAndFans() {

        Long userId = UserContext.getUser();
        //查询粉丝数、关注数
        Map<Object, Object> userDetail = redisTemplate.opsForHash().entries(USER_DETAIL + userId);
        Integer fanNumber = (Integer) userDetail.get("fanNumber");
        Integer followNumber = (Integer) userDetail.get("followNumber");
        if (followNumber == null) {
            followNumber = 0;
        }
        if (fanNumber == null) {
            fanNumber = 0;
        }
        UserFollowInfoVO followInfoVO = new UserFollowInfoVO();
        followInfoVO.setFollowNumber(followNumber);
        followInfoVO.setFanNumber(fanNumber);
        //返回
        return Result.success(followInfoVO);
    }

    @Override
    public Result getUserFollow(ScrollPageDto pageDto) {
        //获取用户ID
        Long userId = UserContext.getUser();

        LocalDateTime minTime = pageDto.getMinTime();
        Long pageSize = pageDto.getPageSize();
        if (minTime == null)
            minTime = LocalDateTime.now();
        if (pageSize == null) {
            pageSize = 12L;
        }
        LambdaQueryWrapper<UserFollow> queryWrapper = Wrappers.lambdaQuery(UserFollow.class)
                .eq(UserFollow::getUserId, userId)
                .lt(UserFollow::getCreateTime, minTime)
                .orderByDesc(UserFollow::getCreateTime)
                .last("limit " + pageSize);
        //查询关注用户
        List<UserFollow> userFollows = userFollowMapper.selectList(queryWrapper);
        if (userFollows.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        //关注对象ID列表
        List<Long> followIds = userFollows.stream().map(UserFollow::getFollowId).collect(Collectors.toList());
        String idsStr = StringUtils.join(followIds, ",");
        //查询关注用户信息
        List<User> users = list(Wrappers.lambdaQuery(User.class)
                .in(User::getId, followIds)
                .last("order by field ( id ," + idsStr + ")"));
        //查询关注者详细信息
        LambdaQueryWrapper<UserDetail> userDetailWrapper = Wrappers.lambdaQuery(UserDetail.class)
                .in(UserDetail::getUserId, followIds)
                .last("order by field ( user_id ," + idsStr + ")");
        List<UserDetail> userDetails = userDetailMapper.selectList(userDetailWrapper);

        ArrayList<UserFollowVO> userFollowVOS = new ArrayList<>();

        //判断对方是否是我的粉丝
        List<Long> fansIds = userFollowMapper.selectList(Wrappers.lambdaQuery(UserFollow.class)
                        .eq(UserFollow::getFollowId, userId)
                        .in(UserFollow::getUserId, followIds))
                .stream()
                .map(UserFollow::getUserId)
                .collect(Collectors.toList());

        for (int i = 0; i < followIds.size(); i++) {

            User user = users.get(i);
            String nickname = user.getNickname();
            String avatar = user.getAvatar();

            UserFollow userFollow = userFollows.get(i);
            Long followId = userFollow.getFollowId();
            LocalDateTime createTime = userFollow.getCreateTime();

            UserDetail userDetail = userDetails.get(i);
            int articleNumber = userDetail.getArticleNumber();
            int fanNumber = userDetail.getFanNumber();

            UserFollowVO followVO = UserFollowVO.builder()
                    .isFollow(true)
                    .isFan(fansIds.contains(followId))
                    .userId(followId)
                    .avatar(avatar)
                    .nickname(nickname)
                    .articleNumber(articleNumber)
                    .fanNumber(fanNumber)
                    .createTime(createTime)
                    .build();

            userFollowVOS.add(followVO);
        }

        return Result.success(userFollowVOS);
    }

    /**
     * 获取粉丝
     *
     * @param pageDto
     * @return
     */
    @Override
    public Result getUserFans(ScrollPageDto pageDto) {
        //获取用户ID
        Long userId = UserContext.getUser();

        LocalDateTime minTime = pageDto.getMinTime();
        Long pageSize = pageDto.getPageSize();
        if (minTime == null)
            minTime = LocalDateTime.now();
        if (pageSize == null) {
            pageSize = 12L;
        }
        LambdaQueryWrapper<UserFollow> queryWrapper = Wrappers.lambdaQuery(UserFollow.class)
                .eq(UserFollow::getFollowId, userId)
                .lt(UserFollow::getCreateTime, minTime)
                .orderByDesc(UserFollow::getCreateTime)
                .last("limit " + pageSize);

        //查询粉丝用户
        List<UserFollow> userFollows = userFollowMapper.selectList(queryWrapper);
        if (userFollows.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        //粉丝ID列表
        List<Long> fansId = userFollows.stream().map(UserFollow::getUserId).collect(Collectors.toList());
        String fansIdStr = StringUtils.join(fansId, ",");

        //查询粉丝用户信息
        List<User> users = list(Wrappers.lambdaQuery(User.class)
                .in(User::getId, fansId)
                .last("order by field ( id ," + fansIdStr + ")"));

        //判断对方是否是我的关注
        List<Long> followIdList = userFollowMapper.selectList(Wrappers.lambdaQuery(UserFollow.class)
                        .eq(UserFollow::getUserId, userId)
                        .in(UserFollow::getFollowId, fansId))
                .stream()
                .map(UserFollow::getFollowId)
                .collect(Collectors.toList());

        ArrayList<UserFanVO> userFanVOS = new ArrayList<>();

        for (int i = 0; i < fansId.size(); i++) {
            User user = users.get(i);

            Long fanId = user.getId();
            String avatar = user.getAvatar();
            String nickname = user.getNickname();
            LocalDateTime createTime = userFollows.get(i).getCreateTime();

            UserFanVO fanVO = UserFanVO.builder()
                    .userId(fanId)
                    .avatar(avatar)
                    .nickname(nickname)
                    .isFan(true)
                    .isFollow(followIdList.contains(fanId))
                    .createTime(createTime)
                    .build();

            userFanVOS.add(fanVO);
        }
        return Result.success(userFanVOS);
    }


    @Override
    public Result handleUserFollow(HandleFollowDto followDto) {
        Long userId = UserContext.getUser();
        Long followUserId = followDto.getFollowId();
        short option = followDto.getOption();
        //处理关系
        if (option == -1) {
            //取关
            if (userFollowMapper.delete(Wrappers.lambdaQuery(UserFollow.class)
                    .eq(UserFollow::getFollowId, followUserId)
                    .eq(UserFollow::getUserId, userId)) == 0) {
                throw new SystemErrorException();
            }

        } else {
            //关注
            UserFollow userFollow = new UserFollow();
            userFollow.setUserId(userId);
            userFollow.setFollowId(followUserId);
            userFollow.setCreateTime(LocalDateTime.now());
            try {
                userFollowMapper.insert(userFollow);
            } catch (Exception ex) {
                log.error("用户关注异常", ex);
                throw new BaseException("关注失败");
            }
        }
        asyncTaskService.handleUserFollowAfter(followUserId,userId,option);

        return Result.success();
    }

    @Override
    public Result updateUser(User user) {
        Long currentUserId = UserContext.getUser();
        User currentUser = getById(currentUserId);

        if (currentUser == null) {
            throw new SystemErrorException();
        }
        //判断
        if (currentUser.getPermission() == 0 && !currentUserId.equals(user.getId())) {
            throw new NoPermissionException();
        }
        //修改
        if (!updateById(user)) {
            throw new ChangeFailedException();
        }
        //异步修改搜索文档
        asyncTaskService.handleUpdateUserAfter(user);
        return Result.success();
    }

    @Override
    public Result removeFans(Long fanId) {
        //当前用户
        Long currentUserId = UserContext.getUser();
        if (userFollowMapper.delete(Wrappers.lambdaQuery(UserFollow.class)
                .eq(UserFollow::getFollowId, currentUserId)
                .eq(UserFollow::getUserId, fanId)) == 0) {
            //失败
            throw new SystemErrorException();
        }
        redisTemplate.opsForHash().increment(USER_DETAIL + currentUserId, "fanNumber", -1);
        redisTemplate.opsForHash().increment(USER_DETAIL + fanId, "followNumber", -1);
        return Result.success();
    }

    @Override
    public Result getUserDetails(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new SystemErrorException();
        }
        UserDetailVO userDetailVO = new UserDetailVO();
        BeanUtils.copyProperties(user, userDetailVO);
        //查询我是否关注了
        UserFollow userFollow = userFollowMapper.selectOne(Wrappers.lambdaQuery(UserFollow.class)
                .eq(UserFollow::getFollowId, userId).eq(UserFollow::getUserId, UserContext.getUser()));
        if (userFollow != null) {
            userDetailVO.setFollow(true);
        }
        //查询用户文章数、粉丝、关注数
        Map<Object, Object> userMap = redisTemplate.opsForHash().entries(USER_DETAIL + userId);
        //文章数
        Object articleNumber = userMap.get("articleNumber");
        //粉丝数
        Object fanNumber = userMap.get("fanNumber");
        //关注数
        Object followNumber = userMap.get("followNumber");
        articleNumber = articleNumber == null ? 0 : articleNumber;
        fanNumber = fanNumber == null ? 0 : fanNumber;
        followNumber = followNumber == null ? 0 : followNumber;

        userDetailVO.setArticleNumber((Integer) articleNumber);
        userDetailVO.setFanNumber((Integer) fanNumber);
        userDetailVO.setFollowNumber((Integer) followNumber);

        return Result.success(userDetailVO);
    }

    @Override
    public Result changePassword(PasswordDto passwordDto) {
        Long currentUserId = UserContext.getUser();
        User user;//查询
        if (currentUserId == null || (user = getById(currentUserId)) == null) {
            throw new SystemErrorException();
        }
        String oldPass = passwordDto.getOldPass();
        String env = System.getenv(KEY_NAME);
        String salt = user.getSalt();
        if (!oldPass.equals(AESUtil.decrypt(user.getPassword(), env + salt))) {
            return Result.error("原密码输入有误");
        }
        //修改密码
        String password = AESUtil.encrypt(passwordDto.getNewPass(), env + salt);
        user.setPassword(password);
        save(user);
        return Result.success();
    }

    private String createCode() {
        StringBuilder codeBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            codeBuilder.append(random.nextInt(10));
        }
        return codeBuilder.toString();
    }
}
