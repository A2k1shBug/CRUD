package com.example.post_info.exception;

public class PostNotSave extends RuntimeException {
    public PostNotSave(String message,Throwable e) {
        super(message,e);
    }
}
