package com.dev.expensemanagement.controller;

import com.dev.expensemanagement.dto.response.ExpenseExtractionResponse;
import com.dev.expensemanagement.service.GeminiExpenseExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/ai/expenses")
@RequiredArgsConstructor
public class AiExpenseController {

    private final GeminiExpenseExtractionService extractionService;

    @PostMapping("/extract-from-receipt")
    public ResponseEntity<?> extractFromReceipt(@RequestParam("image") MultipartFile image) {
        try {
            ExpenseExtractionResponse response = extractionService.extractFromReceipt(image);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(error(e.getMessage()));
        }
    }

    @PostMapping("/extract-from-audio")
    public ResponseEntity<?> extractFromAudio(@RequestParam("audio") MultipartFile audio) {
        try {
            ExpenseExtractionResponse response = extractionService.extractFromAudio(audio);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(500).body(error(e.getMessage()));
        }
    }

    private Map<String, Object> error(String message) {
        return Map.of(
                "success", false,
                "message", message
        );
    }
}
