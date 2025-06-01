package com.openelements.conduct.controller;

import com.openelements.conduct.api.dto.AnalysisDto;
import com.openelements.conduct.service.AnalysisService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    @Autowired
    public AnalysisController(@NonNull AnalysisService analysisService) {
        this.analysisService = Objects.requireNonNull(analysisService, "analysisService must not be null");
    }

    @GetMapping
    public ResponseEntity<AnalysisDto> getAnalysis() {
        AnalysisDto analysis = analysisService.generateAnalysis();
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/trends")
    public ResponseEntity<TrendSummary> getTrends() {
        AnalysisDto analysis = analysisService.generateAnalysis();
        TrendSummary summary = new TrendSummary(
            analysis.trends().trend(),
            analysis.trends().changePercentage(),
            analysis.trends().description(),
            analysis.totalReports(),
            analysis.averageViolationsPerDay()
        );
        return ResponseEntity.ok(summary);
    }

    public record TrendSummary(
        String trend,
        double changePercentage,
        String description,
        long totalReports,
        double averageViolationsPerDay
    ) {}
}
