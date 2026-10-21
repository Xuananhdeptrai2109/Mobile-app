package com.dev.expensemanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWalletRequest {
    @NotBlank(message = "Tên ví không được trống")
    @Size(max = 100, message = "Tên ví tối đa 100 ký tự")
    private String name;

    @JsonProperty("isDefault")
    private Boolean makeDefault;
}
