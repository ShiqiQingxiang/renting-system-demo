package com.rental.common.exception;

import com.rental.common.constant.ResponseCode;

/**
 * 资源未找到异常
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(ResponseCode.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(ResponseCode.NOT_FOUND, String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }

    public static ResourceNotFoundException user(Long userId) {
        return new ResourceNotFoundException("用户", "ID", userId);
    }

    public static ResourceNotFoundException item(Long itemId) {
        return new ResourceNotFoundException("物品", "ID", itemId);
    }

    public static ResourceNotFoundException order(Long orderId) {
        return new ResourceNotFoundException("订单", "ID", orderId);
    }

    public static ResourceNotFoundException role(Long roleId) {
        return new ResourceNotFoundException("角色", "ID", roleId);
    }
}
