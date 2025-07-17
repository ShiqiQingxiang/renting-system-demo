package com.rental.contract.DTO;

import com.rental.contract.model.Contract;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractDTO {

    private Long id;

    private String contractNo;

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    private String orderNo;

    private Long templateId;

    private String templateName;

    @NotBlank(message = "合同内容不能为空")
    private String content;

    private Contract.ContractStatus status;

    private String statusDescription;

    private LocalDateTime signedAt;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 订单相关信息
    private String userName;
    private String userEmail;
    private String itemNames;
    private String rentalPeriod;
}
