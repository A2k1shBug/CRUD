package com.example.post_info.global_exception_handler;

import com.example.api_response.ApiResponse;
import com.example.post_info.exception.PostNotExist;
import com.example.post_info.exception.PostNotSave;
import com.example.response_error.ErrorResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandlerPost {
    @ExceptionHandler(PostNotSave.class)
    public ResponseEntity<ApiResponse<Void>> handleSaveDb(PostNotSave e, HttpServletRequest request) {
        log.error("Fail to user save", e);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(e.getMessage(), errorResponse));
    }

    @ExceptionHandler(PostNotExist.class)
    public ResponseEntity<ApiResponse<Void>> deleteUser(PostNotExist e, HttpServletRequest request) {
        log.error("Fail to delete user", e);
        HttpStatus status = HttpStatus.NOT_FOUND;

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity
                .status(status)
                .body(ApiResponse.failure(e.getMessage(), errorResponse));
    }
}

