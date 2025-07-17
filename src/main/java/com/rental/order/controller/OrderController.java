package com.rental.order.controller;

import com.rental.common.response.ApiResponse;
import com.rental.order.DTO.*;
import com.rental.order.model.Order;
import com.rental.order.service.OrderService;
import com.rental.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单管理相关接口，包括订单的创建、更新、查询、审核、归还等功能")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAuthority('ORDER_CREATE')")
    @Operation(
        summary = "创建订单",
        description = "用户创建新的租赁订单"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "订单创建成功",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "创建订单成功示例",
                value = """
                {
                    "success": true,
                    "message": "订单创建成功",
                    "data": {
                        "id": 1,
                        "orderNo": "ORD20250716000001",
                        "userId": 1,
                        "username": "testuser",
                        "totalAmount": 300.00,
                        "depositAmount": 1000.00,
                        "status": "PENDING",
                        "startDate": "2025-07-20",
                        "endDate": "2025-07-22",
                        "rentalDays": 3
                    }
                }
                """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "订单创建请求信息",
                content = @Content(
                    schema = @Schema(implementation = OrderCreateRequest.class),
                    examples = @ExampleObject(
                        name = "创建订单请求示例",
                        value = """
                        {
                            "startDate": "2025-07-20",
                            "endDate": "2025-07-22",
                            "orderItems": [
                                {
                                    "itemId": 1,
                                    "quantity": 1
                                },
                                {
                                    "itemId": 2,
                                    "quantity": 2
                                }
                            ],
                            "remark": "需要送货上门"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderDto result = orderService.createOrder(request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("订单创建成功", result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE')")
    @Operation(summary = "更新订单", description = "更新订单信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "订单更新成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "订单不存在")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrder(
            @Parameter(description = "订单ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody OrderUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderDto result = orderService.updateOrder(id, request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("订单更新成功", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_VIEW')")
    @Operation(summary = "获取订单详情", description = "根据ID获取订单详细信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "订单不存在")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(
            @Parameter(description = "订单ID", example = "1") @PathVariable Long id) {
        OrderDto result = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/order-no/{orderNo}")
    @PreAuthorize("hasAuthority('ORDER_VIEW')")
    @Operation(summary = "根据订单号获取订单", description = "根据订单号获取订单详细信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "订单不存在")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderByOrderNo(
            @Parameter(description = "订单号", example = "ORD20250716000001") @PathVariable String orderNo) {
        OrderDto result = orderService.getOrderByOrderNo(orderNo);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ORDER_UPDATE')")
    @Operation(summary = "删除订单", description = "删除指定的订单")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "删除成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未认证")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "订单不存在")
    public ResponseEntity<ApiResponse<String>> deleteOrder(
            @Parameter(description = "订单ID", example = "1") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        orderService.deleteOrder(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("订单删除成功"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ORDER_VIEW')")
    @Operation(summary = "搜索订单", description = "根据条件搜索订单")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "搜索成功")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> searchOrders(
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "订单状态") @RequestParam(required = false) Order.OrderStatus status,
            @Parameter(description = "开始日期起") @RequestParam(required = false) LocalDate startDateFrom,
            @Parameter(description = "开始日期止") @RequestParam(required = false) LocalDate startDateTo,
            @Parameter(description = "结束日期起") @RequestParam(required = false) LocalDate endDateFrom,
            @Parameter(description = "结束日期止") @RequestParam(required = false) LocalDate endDateTo,
            @Parameter(description = "最低金额") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "最高金额") @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "物品ID") @RequestParam(required = false) Long itemId,
            @Parameter(description = "物品名称") @RequestParam(required = false) String itemName,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        OrderSearchRequest request = new OrderSearchRequest();
        request.setOrderNo(orderNo);
        request.setUserId(userId);
        request.setUsername(username);
        request.setStatus(status);
        request.setStartDateFrom(startDateFrom);
        request.setStartDateTo(startDateTo);
        request.setEndDateFrom(endDateFrom);
        request.setEndDateTo(endDateTo);
        request.setMinAmount(minAmount);
        request.setMaxAmount(maxAmount);
        request.setItemId(itemId);
        request.setItemName(itemName);
        request.setCategoryId(categoryId);

        Page<OrderDto> result = orderService.searchOrders(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasAuthority('ORDER_VIEW')")
    @Operation(summary = "获取我的订单", description = "获取当前用户的所有订单")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getMyOrders(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderDto> result = orderService.getMyOrders(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ORDER_VIEW')")
    @Operation(summary = "获取用户订单", description = "获取指定用户的所有订单")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getUserOrders(
            @Parameter(description = "用户ID", example = "1") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderDto> result = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('ORDER_UPDATE')")
    @Operation(summary = "确认订单", description = "确认订单，将状态从待确认改为已确认")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "确认成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "订单状态不允许确认")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "订单不存在")
    public ResponseEntity<ApiResponse<OrderDto>> confirmOrder(
            @Parameter(description = "订单ID", example = "1") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderDto result = orderService.confirmOrder(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("订单确认成功", result));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ORDER_CANCEL')")
    @Operation(summary = "取消订单", description = "取消订单")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "取消成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "订单状态不允许取消")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "订单不存在")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @Parameter(description = "订单ID", example = "1") @PathVariable Long id,
            @Parameter(description = "取消原因") @RequestParam(required = false) String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderDto result = orderService.cancelOrder(id, reason, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("订单取消成功", result));
    }

    @PostMapping("/{id}/audit")
    @PreAuthorize("hasAuthority('ORDER_AUDIT')")
    @Operation(summary = "审核订单", description = "审核订单，通过或拒绝")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "审核完成")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "订单状态不允许审核")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "订单不存在")
    public ResponseEntity<ApiResponse<OrderDto>> auditOrder(
            @Parameter(description = "订单ID", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "审核请求信息",
                content = @Content(
                    schema = @Schema(implementation = OrderAuditRequest.class),
                    examples = @ExampleObject(
                        value = """
                        {
                            "approved": true,
                            "comment": "审核通过，物品状态良好"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody OrderAuditRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderDto result = orderService.auditOrder(id, request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("订单审核完成", result));
    }

    @PostMapping("/{id}/start-using")
    @PreAuthorize("hasAuthority('ORDER_UPDATE')")
    @Operation(summary = "开始使用订单", description = "将已支付的订单状态改为使用中")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "开始使用成功")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "订单状态不允许开始使用")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "订单不存在")
    public ResponseEntity<ApiResponse<OrderDto>> startUsingOrder(
            @Parameter(description = "订单ID", example = "1") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderDto result = orderService.startUsingOrder(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("订单开始使用成功", result));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAuthority('ORDER_RETURN')")
    @Operation(summary = "处理订单归还", description = "处理订单物品归还")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "归还处理完成")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "订单状态不允许归还")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "订单不存在")
    public ResponseEntity<ApiResponse<OrderDto>> returnOrder(
            @Parameter(description = "订单ID", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "归还请求信息",
                content = @Content(
                    schema = @Schema(implementation = OrderReturnRequest.class),
                    examples = @ExampleObject(
                        value = """
                        {
                            "returnDate": "2025-07-22",
                            "returnRemark": "物品完好归还",
                            "allItemsReturned": true,
                            "hasDamage": false
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody OrderReturnRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        OrderDto result = orderService.returnOrder(id, request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("订单归还处理完成", result));
    }

    @GetMapping("/pending-audit")
    @PreAuthorize("hasAuthority('ORDER_AUDIT')")
    @Operation(summary = "获取待审核订单", description = "获取所有待审核的订单列表")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getPendingAuditOrders(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDto> result = orderService.getPendingAuditOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/expiring-today")
    @PreAuthorize("hasAuthority('ORDER_VIEW')")
    @Operation(summary = "获取今日到期订单", description = "获取今天到期的订单列表")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getOrdersExpiringToday() {
        List<OrderDto> result = orderService.getOrdersExpiringToday();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAuthority('ORDER_VIEW')")
    @Operation(summary = "获取超期订单", description = "获取已超期未归还的订单列表")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getOverdueOrders() {
        List<OrderDto> result = orderService.getOverdueOrders();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('ORDER_VIEW')")
    @Operation(summary = "获取订单统计信息", description = "获取订单的各种统计数据")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功")
    public ResponseEntity<ApiResponse<OrderStatistics>> getOrderStatistics() {
        OrderStatistics statistics = new OrderStatistics(
            orderService.countOrdersByStatus(null),
            orderService.countOrdersByStatus(Order.OrderStatus.PENDING),
            orderService.countOrdersByStatus(Order.OrderStatus.CONFIRMED),
            orderService.countOrdersByStatus(Order.OrderStatus.PAID),
            orderService.countOrdersByStatus(Order.OrderStatus.IN_USE),
            orderService.countOrdersByStatus(Order.OrderStatus.RETURNED),
            orderService.countOrdersByStatus(Order.OrderStatus.CANCELLED)
        );

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/check-availability")
    @Operation(summary = "检查物品可用性", description = "检查物品在指定时间段是否可租赁")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "检查完成")
    public ResponseEntity<ApiResponse<Boolean>> checkItemAvailability(
            @Parameter(description = "物品ID") @RequestParam Long itemId,
            @Parameter(description = "开始日期") @RequestParam LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam LocalDate endDate) {

        boolean available = orderService.isItemAvailableForRent(itemId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(available));
    }

    // 内部统计类
    public static class OrderStatistics {
        public final long totalOrders;
        public final long pendingOrders;
        public final long confirmedOrders;
        public final long paidOrders;
        public final long inUseOrders;
        public final long returnedOrders;
        public final long cancelledOrders;

        public OrderStatistics(long totalOrders, long pendingOrders, long confirmedOrders,
                             long paidOrders, long inUseOrders, long returnedOrders, long cancelledOrders) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.confirmedOrders = confirmedOrders;
            this.paidOrders = paidOrders;
            this.inUseOrders = inUseOrders;
            this.returnedOrders = returnedOrders;
            this.cancelledOrders = cancelledOrders;
        }
    }
}
