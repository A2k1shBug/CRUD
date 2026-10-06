package com.example.user_info.exception;

public class UserNotExist extends RuntimeException {
    public UserNotExist(String message,Throwable e) {
        super(message,e);
    }
}
