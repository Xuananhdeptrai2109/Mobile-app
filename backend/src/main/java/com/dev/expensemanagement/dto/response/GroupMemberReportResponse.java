package com.dev.expensemanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMemberReportResponse {
    private Long memberId;
    private String fullName;
    private BigDecimal totalPaid;
    private BigDecimal totalOwed;
    private BigDecimal settledAmount;
    private BigDecimal outstandingAmount;
    private BigDecimal netBalance;
}
