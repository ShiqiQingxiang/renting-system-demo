package com.rental.payment.DTO;

import com.rental.payment.model.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "支付记录响应")
public class PaymentRecordDto {

    @Schema(description = "记录ID", example = "1")
    private Long id;

    @Schema(description = "支付ID", example = "1")
    private Long paymentId;

    @Schema(description = "支付单号", example = "PAY20250716000001")
    private String paymentNo;

    @Schema(description = "状态", example = "SUCCESS")
    private Payment.PaymentStatus status;

    @Schema(description = "状态描述", example = "支付成功")
    private String statusDesc;

    @Schema(description = "响应数据")
    private String responseData;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
