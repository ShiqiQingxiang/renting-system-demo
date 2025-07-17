package com.rental.item.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemApprovalRequest {

    @NotNull(message = "审核结果不能为空")
    private Boolean approved; // true为通过，false为拒绝

    @NotBlank(message = "审核意见不能为空")
    private String comment;
}
