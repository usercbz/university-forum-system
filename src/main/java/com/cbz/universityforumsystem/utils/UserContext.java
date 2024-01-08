package com.cbz.universityforumsystem.utils;


public class UserContext {

    private static ThreadLocal<Long> tl = new ThreadLocal<>();

    public static void setUser(Long userId){
        tl.set(userId);
    }

    public static Long getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
