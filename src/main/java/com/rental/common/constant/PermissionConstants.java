package com.rental.common.constant;

/**
 * 权限常量定义
 */
public class PermissionConstants {

    // 用户管理权限
    public static final String USER_VIEW = "USER_VIEW";
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";
    public static final String USER_ASSIGN_ROLE = "USER_ASSIGN_ROLE";
    public static final String USER_RESET_PASSWORD = "USER_RESET_PASSWORD";

    // 物品管理权限
    public static final String ITEM_VIEW = "ITEM_VIEW";
    public static final String ITEM_CREATE = "ITEM_CREATE";
    public static final String ITEM_UPDATE = "ITEM_UPDATE";
    public static final String ITEM_DELETE = "ITEM_DELETE";
    public static final String ITEM_AUDIT = "ITEM_AUDIT";
    public static final String ITEM_PUBLISH = "ITEM_PUBLISH";
    public static final String ITEM_OFFLINE = "ITEM_OFFLINE";

    // 订单管理权限
    public static final String ORDER_VIEW = "ORDER_VIEW";
    public static final String ORDER_CREATE = "ORDER_CREATE";
    public static final String ORDER_UPDATE = "ORDER_UPDATE";
    public static final String ORDER_CANCEL = "ORDER_CANCEL";
    public static final String ORDER_AUDIT = "ORDER_AUDIT";
    public static final String ORDER_COMPLETE = "ORDER_COMPLETE";
    public static final String ORDER_REFUND = "ORDER_REFUND";

    // 合同管理权限
    public static final String CONTRACT_VIEW = "CONTRACT_VIEW";
    public static final String CONTRACT_CREATE = "CONTRACT_CREATE";
    public static final String CONTRACT_UPDATE = "CONTRACT_UPDATE";
    public static final String CONTRACT_DELETE = "CONTRACT_DELETE";
    public static final String CONTRACT_SIGN = "CONTRACT_SIGN";
    public static final String CONTRACT_AUDIT = "CONTRACT_AUDIT";

    // 支付管理权限
    public static final String PAYMENT_VIEW = "PAYMENT_VIEW";
    public static final String PAYMENT_PROCESS = "PAYMENT_PROCESS";
    public static final String PAYMENT_REFUND = "PAYMENT_REFUND";
    public static final String PAYMENT_AUDIT = "PAYMENT_AUDIT";

    // 财务管理权限
    public static final String FINANCE_VIEW = "FINANCE_VIEW";
    public static final String FINANCE_REPORT = "FINANCE_REPORT";
    public static final String FINANCE_EXPORT = "FINANCE_EXPORT";
    public static final String FINANCE_AUDIT = "FINANCE_AUDIT";

    // 角色权限管理
    public static final String ROLE_VIEW = "ROLE_VIEW";
    public static final String ROLE_CREATE = "ROLE_CREATE";
    public static final String ROLE_UPDATE = "ROLE_UPDATE";
    public static final String ROLE_DELETE = "ROLE_DELETE";
    public static final String PERMISSION_VIEW = "PERMISSION_VIEW";
    public static final String PERMISSION_ASSIGN = "PERMISSION_ASSIGN";

    // 系统管理权限
    public static final String SYSTEM_CONFIG = "SYSTEM_CONFIG";
    public static final String SYSTEM_LOG = "SYSTEM_LOG";
    public static final String SYSTEM_MONITOR = "SYSTEM_MONITOR";
    public static final String SYSTEM_BACKUP = "SYSTEM_BACKUP";

    // 评价管理权限
    public static final String REVIEW_VIEW = "REVIEW_VIEW";
    public static final String REVIEW_DELETE = "REVIEW_DELETE";
    public static final String REVIEW_REPLY = "REVIEW_REPLY";
    public static final String REVIEW_AUDIT = "REVIEW_AUDIT";

    // 通知管理权限
    public static final String NOTIFICATION_VIEW = "NOTIFICATION_VIEW";
    public static final String NOTIFICATION_SEND = "NOTIFICATION_SEND";
    public static final String NOTIFICATION_DELETE = "NOTIFICATION_DELETE";
}
