package com.rental.common.exception;

import com.rental.common.constant.ResponseCode;
import lombok.Getter;

/**
 * 业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResponseCode.INTERNAL_SERVER_ERROR;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResponseCode.INTERNAL_SERVER_ERROR;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    // 静态工厂方法，便于创建常见异常
    public static BusinessException userNotFound() {
        return new BusinessException(ResponseCode.USER_NOT_FOUND, ResponseCode.USER_NOT_FOUND_MSG);
    }

    public static BusinessException itemNotFound() {
        return new BusinessException(ResponseCode.ITEM_NOT_FOUND, ResponseCode.ITEM_NOT_FOUND_MSG);
    }

    public static BusinessException orderNotFound() {
        return new BusinessException(ResponseCode.ORDER_NOT_FOUND, ResponseCode.ORDER_NOT_FOUND_MSG);
    }

    public static BusinessException paymentFailed() {
        return new BusinessException(ResponseCode.PAYMENT_FAILED, ResponseCode.PAYMENT_FAILED_MSG);
    }

    public static BusinessException permissionDenied() {
        return new BusinessException(ResponseCode.PERMISSION_DENIED, ResponseCode.PERMISSION_DENIED_MSG);
    }

    public static BusinessException invalidCredentials() {
        return new BusinessException(ResponseCode.INVALID_CREDENTIALS, ResponseCode.INVALID_CREDENTIALS_MSG);
    }
}
