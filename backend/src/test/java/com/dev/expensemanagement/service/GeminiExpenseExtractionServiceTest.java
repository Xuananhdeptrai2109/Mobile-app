package com.dev.expensemanagement.service;

import com.dev.expensemanagement.dto.response.ExpenseExtractionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeminiExpenseExtractionServiceTest {

    private GeminiExpenseExtractionService service;

    @BeforeEach
    void setUp() {
        service = new GeminiExpenseExtractionService();
        ReflectionTestUtils.setField(service, "apiKey", "test-key");
    }

    @Test
    void rejectsFileWhoseBytesDoNotMatchDeclaredMimeType() {
        MockMultipartFile fakePng = new MockMultipartFile(
                "image", "receipt.png", "image/png", "not-a-png".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> service.extractFromReceipt(fakePng));
    }

    @Test
    void parsesFencedJsonAndNormalizesInvalidValues() {
        String response = """
                ```json
                {"name":"Bua toi","amount":"1.000.000 VND","expenseDate":"31/12/2026"}
                ```
                """;

        ExpenseExtractionResponse result = ReflectionTestUtils.invokeMethod(
                service, "parseExtraction", response
        );

        assertEquals("Bua toi", result.getName());
        assertEquals(new BigDecimal("1000000"), result.getAmount());
        assertNull(result.getExpenseDate());
    }

    @Test
    void dropsNonPositiveAmount() {
        ExpenseExtractionResponse result = ReflectionTestUtils.invokeMethod(
                service,
                "parseExtraction",
                "{\"name\":\"Refund\",\"amount\":-10}"
        );

        assertNull(result.getAmount());
    }

    @Test
    void roundsExtractedVndAmountToWholeUnit() {
        ExpenseExtractionResponse result = ReflectionTestUtils.invokeMethod(
                service,
                "parseExtraction",
                "{\"name\":\"Dinner\",\"amount\":1000.6}"
        );

        assertEquals(new BigDecimal("1001"), result.getAmount());
    }
}
