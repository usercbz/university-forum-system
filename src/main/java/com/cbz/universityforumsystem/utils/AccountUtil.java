package com.cbz.universityforumsystem.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.cbz.universityforumsystem.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.cbz.universityforumsystem.constant.RedisConstant.USER_ACCOUNT_LIST;

@Component
@Slf4j
public class AccountUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取账号
     *
     * @return 新账号
     */
    public String get() {
        Object o = redisTemplate.opsForList().leftPop(USER_ACCOUNT_LIST);
        if (o == null) {
            createAccountList();
            //递归调用
            return get();
        } else {
            return (String) o;
        }
    }

    public void createAccountList() {
        //创建一批账号  30个
        List<String> accounts = getAccounts();
        //查询 数据库中的账号是否重复
        List<String> duplicateAccount = userMapper.selectDuplicateAccount(accounts);
        //重复 去重
        if (!CollectionUtils.isEmpty(duplicateAccount)) {
            accounts = accounts.stream().filter(item -> !duplicateAccount.contains(item)).collect(Collectors.toList());
        }
        //存入Redis中
        redisTemplate.opsForList().rightPushAll(USER_ACCOUNT_LIST, accounts.toArray());
    }

    @NotNull
    private static List<String> getAccounts() {
        List<String> accounts = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 30; i++) {
            //创建账号逻辑 12位
            StringBuilder account = new StringBuilder();
            //首位不能为0
            account.append((random.nextInt(9) + 1));
            for (int j = 0; j < 11; j++) {
                //后续 0-9 填充
                account.append(random.nextInt(10));
            }
            //存入集合
            accounts.add(account.toString());
        }
        return accounts;
    }
}
