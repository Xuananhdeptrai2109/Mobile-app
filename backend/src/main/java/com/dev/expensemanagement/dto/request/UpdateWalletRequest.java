package com.dev.expensemanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWalletRequest {
    @Size(min = 1, max = 100, message = "Tên ví phải từ 1 đến 100 ký tự")
    private String name;

    @JsonProperty("isDefault")
    private Boolean makeDefault;
}
