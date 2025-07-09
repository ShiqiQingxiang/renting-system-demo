package com.rental.common.response;

import com.rental.common.constant.ResponseCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一API响应格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String path;

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ResponseCode.SUCCESS, ResponseCode.SUCCESS_MSG, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS, ResponseCode.SUCCESS_MSG, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS, message, data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(ResponseCode.INTERNAL_SERVER_ERROR, message, null);
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(ResponseCode.BAD_REQUEST, message, null);
    }

    public static <T> ApiResponse<T> unauthorized() {
        return new ApiResponse<>(ResponseCode.UNAUTHORIZED, ResponseCode.UNAUTHORIZED_MSG, null);
    }

    public static <T> ApiResponse<T> forbidden() {
        return new ApiResponse<>(ResponseCode.FORBIDDEN, ResponseCode.FORBIDDEN_MSG, null);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(ResponseCode.NOT_FOUND, message, null);
    }

    public ApiResponse<T> path(String path) {
        this.path = path;
        return this;
    }
}
