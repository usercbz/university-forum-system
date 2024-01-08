package com.cbz.universityforumsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.cbz.universityforumsystem.mapper")
@EnableAsync//开启异步处理
@EnableTransactionManagement//开启事务
public class UniversityForumSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniversityForumSystemApplication.class, args);
    }

}
