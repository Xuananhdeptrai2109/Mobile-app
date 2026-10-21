package com.dev.expensemanagement.dto;

import com.dev.expensemanagement.dto.request.CreateWalletRequest;
import com.dev.expensemanagement.dto.response.GroupExpenseSplitResponse;
import com.dev.expensemanagement.dto.response.GroupTaskResponse;
import com.dev.expensemanagement.dto.response.WalletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupResponseSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void splitPaymentStatusUsesAndroidContractName() throws Exception {
        var response = GroupExpenseSplitResponse.builder()
                .isPaid(true)
                .build();

        var json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertTrue(json.path("isPaid").asBoolean());
        assertFalse(json.has("paid"));
    }

    @Test
    void taskStatusUsesAndroidContractName() throws Exception {
        var response = GroupTaskResponse.builder()
                .isCompleted(true)
                .build();

        var json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertTrue(json.path("isCompleted").asBoolean());
        assertFalse(json.has("completed"));
    }

    @Test
    void walletDefaultFlagUsesPublicApiContractName() throws Exception {
        var response = WalletResponse.builder().isDefault(true).build();
        var json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertTrue(json.path("isDefault").asBoolean());
        assertFalse(json.has("default"));

        var request = objectMapper.readValue(
                "{\"name\":\"Tiền mặt\",\"isDefault\":true}",
                CreateWalletRequest.class
        );
        assertTrue(request.getMakeDefault());
    }
}
