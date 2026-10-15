package com.example.api_response;

import com.example.response_error.ErrorResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String message;
    private final T data;
    private final ErrorResponse error;

    private ApiResponse(String message, T data, ErrorResponse error) {
        this.message = message;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data, null);
    }

    public static <T> ApiResponse<T> failure(String message, ErrorResponse error) {
        return new ApiResponse<>(message, null, error);
    }

}
