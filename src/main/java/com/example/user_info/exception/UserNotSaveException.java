package com.example.user_info.exception;

public class UserNotSaveException extends RuntimeException {
    public UserNotSaveException(String message,Throwable e) {
        super(message,e);
    }
}
