package com.openelements.conduct.api.dto;

import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;
import java.util.Map;

public record AnalysisDto(
    @NonNull Map<String, Long> violationsByState,
    @NonNull Map<String, Long> violationsBySeverity,
    @NonNull Map<String, Long> violationsByHour,
    @NonNull Map<String, Long> violationsByDay,
    double averageViolationsPerDay,
    @NonNull String mostCommonViolationType,
    @NonNull LocalDateTime analysisTimestamp,
    long totalReports,
    @NonNull TrendAnalysis trends
) {}
