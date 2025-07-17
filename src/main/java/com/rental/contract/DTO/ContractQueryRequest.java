package com.rental.contract.DTO;

import com.rental.contract.model.Contract;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractQueryRequest {

    private String keyword; // 合同编号或用户名

    private Contract.ContractStatus status;

    private Long orderId;

    private Long userId;

    private LocalDate signedDateStart;

    private LocalDate signedDateEnd;

    private LocalDate expiryDateStart;

    private LocalDate expiryDateEnd;

    private int page = 0;

    private int size = 10;

    private String sortBy = "createdAt";

    private String sortDir = "desc";
}
