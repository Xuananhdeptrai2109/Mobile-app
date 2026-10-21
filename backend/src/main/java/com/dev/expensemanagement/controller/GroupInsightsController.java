package com.dev.expensemanagement.controller;

import com.dev.expensemanagement.security.CurrentUserService;
import com.dev.expensemanagement.service.GroupActivityService;
import com.dev.expensemanagement.service.GroupReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/groups/{groupId}")
@RequiredArgsConstructor
public class GroupInsightsController {
    private final GroupActivityService activityService;
    private final GroupReportService reportService;
    private final CurrentUserService currentUserService;

    @GetMapping("/activities")
    public ResponseEntity<?> activities(@PathVariable Long groupId) {
        try {
            return ResponseEntity.ok(activityService.getActivities(groupId, currentUserService.requireUserId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @GetMapping("/report")
    public ResponseEntity<?> report(@PathVariable Long groupId) {
        try {
            return ResponseEntity.ok(reportService.getReport(groupId, currentUserService.requireUserId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @GetMapping(value = "/report.csv", produces = "text/csv")
    public ResponseEntity<?> reportCsv(@PathVariable Long groupId) {
        try {
            byte[] content = reportService.exportCsv(groupId, currentUserService.requireUserId())
                    .getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=group-report-" + groupId + ".csv")
                    .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                    .body(content);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    private Map<String, Object> error(String message) {
        return Map.of("success", false, "message", message);
    }
}
