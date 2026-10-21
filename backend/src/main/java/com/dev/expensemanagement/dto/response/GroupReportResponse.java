package com.dev.expensemanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupReportResponse {
    private Long groupId;
    private String groupName;
    private BigDecimal budget;
    private BigDecimal totalExpense;
    private BigDecimal settledAmount;
    private BigDecimal outstandingAmount;
    private int expenseCount;
    private int taskCount;
    private int completedTaskCount;
    private List<GroupMemberReportResponse> members;
}
