package com.cbz.universityforumsystem;

import com.cbz.universityforumsystem.utils.AccountUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class UniversityForumSystemApplicationTests {

    @Autowired
    private AccountUtil accountUtil;

    @Test
    void contextLoads() {
        log.info(accountUtil.get());
    }

}
