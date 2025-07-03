package com.example.rentingsystemdemo.controller;

import com.example.rentingsystemdemo.DTO.ItemDTO;
import com.example.rentingsystemdemo.DTO.RentalOrderDTO;
import com.example.rentingsystemdemo.model.Item;
import com.example.rentingsystemdemo.model.RentalOrder;
import com.example.rentingsystemdemo.model.User;
import com.example.rentingsystemdemo.repository.ItemRepository;
import com.example.rentingsystemdemo.security.CustomUserDetails;
import com.example.rentingsystemdemo.service.RentalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rental")
public class RentalController {
    private final ItemRepository itemRepository;
    private final RentalService rentalService;

    public RentalController(ItemRepository itemRepository, RentalService rentalService) {
        this.itemRepository = itemRepository;
        this.rentalService = rentalService;
    }

    // 获取所有可用物品
    @GetMapping("/items")
    public ResponseEntity<List<ItemDTO>> listItems() {
        return ResponseEntity.ok(rentalService.getAllAvailableItems());
    }

    // 搜索物品
    @GetMapping("/search")
    public ResponseEntity<List<ItemDTO>> searchItems(@RequestParam String keyword) {
        return ResponseEntity.ok(rentalService.searchItems(keyword));
    }

    // 获取用户拥有的物品
    @GetMapping("/my-items")
    public ResponseEntity<List<ItemDTO>> getMyItems(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = customUserDetails.getUser();
        return ResponseEntity.ok(rentalService.getItems(user.getId()));
    }

    // 租赁物品
    @PostMapping("/rent")
    public ResponseEntity<?> rentItem(
            @RequestBody RentRequest rentRequest, // 用DTO接收参数
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            User user = customUserDetails.getUser();
            Item item = itemRepository.findById(rentRequest.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("物品不存在"));

            RentalOrderDTO orderDTO = rentalService.createOrder(
                    item,
                    user,
                    LocalDate.parse(rentRequest.getStartDate()),
                    LocalDate.parse(rentRequest.getEndDate())
            );
            return ResponseEntity.ok(orderDTO); // 返回DTO
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 获取用户订单
    @GetMapping("/orders")
    public ResponseEntity<List<RentalOrder>> getUserOrders(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User user = customUserDetails.getUser();
        return ResponseEntity.ok(rentalService.getUserOrders(user));
    }

    // 完成订单
    @PostMapping("/complete-order/{id}")
    public ResponseEntity<RentalOrder> completeOrder(@PathVariable Long id) {
        return ResponseEntity.ok(rentalService.completeOrder(id));
    }

    // 取消订单
    @PostMapping("/cancel-order/{id}")
    public ResponseEntity<RentalOrder> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(rentalService.cancelOrder(id));
    }

    // 添加新物品
    @PostMapping("/add-item")
    public ResponseEntity<Item> addItem(
            @RequestBody Item item,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        if (customUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = customUserDetails.getUser();
        item.setOwner(user);
        item.setAvailable(true);
        return ResponseEntity.ok(itemRepository.save(item));
    }

    private static class RentRequest {
        private Long itemId;
        private String startDate;
        private String endDate;

        // Getters and Setters
        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }
}
