package com.example.post_info.exception;

public class PostNotExist extends RuntimeException {
    public PostNotExist(String message) {
        super(message);
    }
}
