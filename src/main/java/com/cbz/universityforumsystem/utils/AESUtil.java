package com.cbz.universityforumsystem.utils;

import com.cbz.universityforumsystem.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Slf4j
public class AESUtil {
    private static final String ALGORITHM = "AES";
    private static final String CHARSET_NAME = StandardCharsets.UTF_8.name();

    /**
     * AES加密
     *
     * @param text 加密内容
     * @param key  秘钥
     * @return 密文
     */
    public static String encrypt(String text, String key) {
        try {
            //实例
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            //创建加密规则
            //创建KeyGenerator 对象，用于生成key
            KeyGenerator keyGenerator = getKeyGenerator(key);
            //加密秘钥
            SecretKey secretKey = new SecretKeySpec(keyGenerator.generateKey().getEncoded(), ALGORITHM);
            //加密模式
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] bytes = cipher.doFinal(text.getBytes(CHARSET_NAME));
            return Base64.encodeBase64String(bytes);
        } catch (Exception e) {
            log.error("加密过程出现错误", e);
            throw new BaseException("服务器错误");
        }
    }

    @NotNull
    private static KeyGenerator getKeyGenerator(String key) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(key.getBytes(CHARSET_NAME));
        keyGenerator.init(128, secureRandom);
        return keyGenerator;
    }

    /**
     * 解密
     *
     * @param ciphertext 密文
     * @param key        秘钥
     * @return 明文
     */
    public static String decrypt(String ciphertext, String key) {
        //实例
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            KeyGenerator keyGenerator = getKeyGenerator(key);
            //加密秘钥
            SecretKey secretKey = new SecretKeySpec(keyGenerator.generateKey().getEncoded(), ALGORITHM);
            //解密模式
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] bytes = cipher.doFinal(Base64.decodeBase64(ciphertext));
            return new String(bytes, CHARSET_NAME);
        } catch (Exception e) {
            log.error("解密过程出现错误", e);
            throw new BaseException("服务器错误");
        }
    }

}
