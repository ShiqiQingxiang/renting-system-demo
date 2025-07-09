package com.rental.common.constant;

/**
 * 响应状态码常量定义
 */
public class ResponseCode {
    // 成功状态码
    public static final int SUCCESS = 200;
    public static final String SUCCESS_MSG = "操作成功";

    // 客户端错误状态码 (4xx)
    public static final int BAD_REQUEST = 400;
    public static final String BAD_REQUEST_MSG = "请求参数错误";

    public static final int UNAUTHORIZED = 401;
    public static final String UNAUTHORIZED_MSG = "未授权访问";

    public static final int FORBIDDEN = 403;
    public static final String FORBIDDEN_MSG = "访问被禁止";

    public static final int NOT_FOUND = 404;
    public static final String NOT_FOUND_MSG = "资源不存在";

    public static final int METHOD_NOT_ALLOWED = 405;
    public static final String METHOD_NOT_ALLOWED_MSG = "请求方法不支持";

    public static final int CONFLICT = 409;
    public static final String CONFLICT_MSG = "资源冲突";

    public static final int VALIDATION_ERROR = 422;
    public static final String VALIDATION_ERROR_MSG = "数据验证失败";

    // 服务器错误状态码 (5xx)
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final String INTERNAL_SERVER_ERROR_MSG = "系统内部错误";

    public static final int SERVICE_UNAVAILABLE = 503;
    public static final String SERVICE_UNAVAILABLE_MSG = "服务暂不可用";

    // 业务错误状态码 (1xxx - 用户相关)
    public static final int USER_NOT_FOUND = 1001;
    public static final String USER_NOT_FOUND_MSG = "用户不存在";

    public static final int USER_ALREADY_EXISTS = 1002;
    public static final String USER_ALREADY_EXISTS_MSG = "用户已存在";

    public static final int INVALID_CREDENTIALS = 1003;
    public static final String INVALID_CREDENTIALS_MSG = "用户名或密码错误";

    public static final int ACCOUNT_DISABLED = 1004;
    public static final String ACCOUNT_DISABLED_MSG = "账户已被禁用";

    public static final int ACCOUNT_LOCKED = 1005;
    public static final String ACCOUNT_LOCKED_MSG = "账户已被锁定";

    public static final int PASSWORD_EXPIRED = 1006;
    public static final String PASSWORD_EXPIRED_MSG = "密码已过期";

    // 物品相关错误 (2xxx)
    public static final int ITEM_NOT_FOUND = 2001;
    public static final String ITEM_NOT_FOUND_MSG = "物品不存在";

    public static final int ITEM_NOT_AVAILABLE = 2002;
    public static final String ITEM_NOT_AVAILABLE_MSG = "物品不可用";

    public static final int ITEM_ALREADY_RENTED = 2003;
    public static final String ITEM_ALREADY_RENTED_MSG = "物品已被租用";

    public static final int ITEM_AUDIT_FAILED = 2004;
    public static final String ITEM_AUDIT_FAILED_MSG = "物品审核失败";

    public static final int ITEM_CATEGORY_NOT_FOUND = 2005;
    public static final String ITEM_CATEGORY_NOT_FOUND_MSG = "物品分类不存在";

    // 订单相关错误 (3xxx)
    public static final int ORDER_NOT_FOUND = 3001;
    public static final String ORDER_NOT_FOUND_MSG = "订单不存在";

    public static final int ORDER_STATUS_ERROR = 3002;
    public static final String ORDER_STATUS_ERROR_MSG = "订单状态错误";

    public static final int ORDER_CANNOT_CANCEL = 3003;
    public static final String ORDER_CANNOT_CANCEL_MSG = "订单无法取消";

    public static final int ORDER_EXPIRED = 3004;
    public static final String ORDER_EXPIRED_MSG = "订单已过期";

    public static final int ORDER_ALREADY_PAID = 3005;
    public static final String ORDER_ALREADY_PAID_MSG = "订单已支付";

    // 支付相关错误 (4xxx)
    public static final int PAYMENT_FAILED = 4001;
    public static final String PAYMENT_FAILED_MSG = "支付失败";

    public static final int PAYMENT_TIMEOUT = 4002;
    public static final String PAYMENT_TIMEOUT_MSG = "支付超时";

    public static final int INSUFFICIENT_BALANCE = 4003;
    public static final String INSUFFICIENT_BALANCE_MSG = "余额不足";

    public static final int PAYMENT_NOT_FOUND = 4004;
    public static final String PAYMENT_NOT_FOUND_MSG = "支付记录不存在";

    public static final int REFUND_FAILED = 4005;
    public static final String REFUND_FAILED_MSG = "退款失败";

    // 合同相关错误 (5xxx)
    public static final int CONTRACT_NOT_FOUND = 5001;
    public static final String CONTRACT_NOT_FOUND_MSG = "合同不存在";

    public static final int CONTRACT_ALREADY_SIGNED = 5002;
    public static final String CONTRACT_ALREADY_SIGNED_MSG = "合同已签署";

    public static final int CONTRACT_EXPIRED = 5003;
    public static final String CONTRACT_EXPIRED_MSG = "合同已过期";

    // 权限相关错误 (6xxx)
    public static final int ROLE_NOT_FOUND = 6001;
    public static final String ROLE_NOT_FOUND_MSG = "角色不存在";

    public static final int PERMISSION_DENIED = 6002;
    public static final String PERMISSION_DENIED_MSG = "权限不足";

    public static final int ROLE_ALREADY_EXISTS = 6003;
    public static final String ROLE_ALREADY_EXISTS_MSG = "角色已存在";

    // 文件相关错误 (7xxx)
    public static final int FILE_UPLOAD_FAILED = 7001;
    public static final String FILE_UPLOAD_FAILED_MSG = "文件上传失败";

    public static final int FILE_SIZE_EXCEEDED = 7002;
    public static final String FILE_SIZE_EXCEEDED_MSG = "文件大小超出限制";

    public static final int FILE_TYPE_NOT_SUPPORTED = 7003;
    public static final String FILE_TYPE_NOT_SUPPORTED_MSG = "文件类型不支持";

    public static final int FILE_NOT_FOUND = 7004;
    public static final String FILE_NOT_FOUND_MSG = "文件不存在";
}
