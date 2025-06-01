package com.openelements.conduct.controller;

import com.openelements.conduct.api.dto.PagedResponse;
import com.openelements.conduct.api.dto.ViolationReportDto;
import com.openelements.conduct.data.ViolationState;
import com.openelements.conduct.service.ViolationReportService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/violation-reports")
public class ViolationReportController {

    private final ViolationReportService violationReportService;

    @Autowired
    public ViolationReportController(@NonNull ViolationReportService violationReportService) {
        this.violationReportService = Objects.requireNonNull(violationReportService, "violationReportService must not be null");
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ViolationReportDto>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) ViolationState violationState,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        // Validate pagination parameters
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        
        PagedResponse<ViolationReportDto> response = violationReportService.getReports(
            page, size, sortBy, sortDir, violationState, severity, startDate, endDate
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViolationReportDto> getReportById(@PathVariable String id) {
        return violationReportService.getReportById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<ReportStats> getStats() {
        // This could be expanded to include more detailed statistics
        return ResponseEntity.ok(new ReportStats("Statistics endpoint - implement as needed"));
    }

    public record ReportStats(String message) {}
}
