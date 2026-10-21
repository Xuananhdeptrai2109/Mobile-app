package com.dev.expensemanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupExpenseSplitResponse {
    private Long splitId;
    private Long memberId;
    private String fullName;
    private BigDecimal splitAmount;
    @Getter(onMethod_ = @JsonProperty("isPaid"))
    private boolean isPaid;
}
