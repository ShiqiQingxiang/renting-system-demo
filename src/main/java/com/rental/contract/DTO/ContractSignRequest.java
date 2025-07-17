package com.rental.contract.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractSignRequest {

    @NotNull(message = "合同ID不能为空")
    private Long contractId;

    private String signatureData;

    private String remarks;
}
