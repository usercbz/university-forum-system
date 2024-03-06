package com.cbz.universityforumsystem.utils;

import java.util.List;

public class MyStringUtils {

    public static String contentMask(String content, List<String> words) {
        if (content == null) {
            return "";
        }
        if (words == null || words.isEmpty()){
            return content;
        }
        //替换
        for (String word : words) {
            StringBuilder sb = new StringBuilder();
            int length = word.length();
            for (int i = 0; i < length; i++) {
                sb.append('*');
            }
            content = content.replace(word, sb.toString());
        }
        return content;
    }
}
