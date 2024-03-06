package com.cbz.universityforumsystem;

import com.cbz.universityforumsystem.constant.SystemConstant;
import com.cbz.universityforumsystem.utils.AESUtil;
import com.cbz.universityforumsystem.vo.CommentReplyVO;
import com.cbz.universityforumsystem.vo.CommentVO;
import org.junit.jupiter.api.Test;



public class SimpleTest {
    @Test
    public void test() {
        String encrypt = AESUtil.encrypt("加密Java", "1234");
        System.out.println("加密后的数据:" + encrypt);
        System.out.println("解密后的数据:" + AESUtil.decrypt(encrypt, "1234"));
    }

    @Test
    public void test2(){
        System.out.println(System.getenv(SystemConstant.KEY_NAME));
    }

    @Test
    public void test3(){
        String str  = "cnsuinoak291e8208d";

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
                System.out.print(str.charAt(i));
            }
        }

    }
}
