package com.rental.common.config;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 自定义分页参数解析器
 * 用于处理前端发送的错误排序参数格式
 */
public class CustomPageableResolver extends PageableHandlerMethodArgumentResolver {

    public CustomPageableResolver() {
        super(new SortHandlerMethodArgumentResolver());
    }

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter,
                                   ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest,
                                   WebDataBinderFactory binderFactory) {

        // 获取分页参数
        String page = webRequest.getParameter("page");
        String size = webRequest.getParameter("size");
        String[] sortParams = webRequest.getParameterValues("sort");

        int pageNumber = 0;
        int pageSize = 20; // 默认页面大小

        // 解析页码
        if (page != null) {
            try {
                pageNumber = Integer.parseInt(page);
            } catch (NumberFormatException e) {
                pageNumber = 0;
            }
        }

        // 解析页面大小
        if (size != null) {
            try {
                pageSize = Integer.parseInt(size);
                // 限制页面大小
                if (pageSize > 100) {
                    pageSize = 100;
                }
                if (pageSize < 1) {
                    pageSize = 20;
                }
            } catch (NumberFormatException e) {
                pageSize = 20;
            }
        }

        // 处理排序参数
        Sort sort = Sort.unsorted();
        if (sortParams != null && sortParams.length > 0) {
            sort = parseSort(sortParams);
        }

        return PageRequest.of(pageNumber, pageSize, sort);
    }

    /**
     * 解析排序参数，处理各种可能的格式
     */
    private Sort parseSort(String[] sortParams) {
        try {
            for (String sortParam : sortParams) {
                if (sortParam == null || sortParam.trim().isEmpty()) {
                    continue;
                }

                // 跳过错误的JSON数组格式
                if (sortParam.startsWith("[") || sortParam.startsWith("{")) {
                    continue;
                }

                // 解析标准格式：property,direction
                String[] parts = sortParam.split(",");
                if (parts.length >= 1) {
                    String property = parts[0].trim();
                    Sort.Direction direction = Sort.Direction.DESC; // 默认降序

                    if (parts.length > 1) {
                        String directionStr = parts[1].trim().toLowerCase();
                        if ("asc".equals(directionStr)) {
                            direction = Sort.Direction.ASC;
                        }
                    }

                    // 验证字段名是否合法（避免注入攻击）
                    if (isValidSortProperty(property)) {
                        return Sort.by(direction, property);
                    }
                }
            }
        } catch (Exception e) {
            // 如果解析失败，返回默认排序
        }

        // 返回默认排序（按ID降序）
        return Sort.by(Sort.Direction.DESC, "id");
    }

    /**
     * 验证排序字段是否合法
     */
    private boolean isValidSortProperty(String property) {
        // 定义允许的排序字段
        String[] allowedProperties = {
            "id", "recordNo", "type", "category", "amount", "createdAt",
            "reportType", "periodStart", "periodEnd", "totalIncome", "totalExpense"
        };

        for (String allowed : allowedProperties) {
            if (allowed.equals(property)) {
                return true;
            }
        }

        return false;
    }
}
