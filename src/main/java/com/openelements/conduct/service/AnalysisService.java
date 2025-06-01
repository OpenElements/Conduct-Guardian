package com.openelements.conduct.service;

import com.openelements.conduct.api.dto.AnalysisDto;
import com.openelements.conduct.api.dto.TrendAnalysis;
import com.openelements.conduct.repository.ViolationReport;
import com.openelements.conduct.repository.ViolationReportRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private final ViolationReportRepository repository;

    @Autowired
    public AnalysisService(@NonNull ViolationReportRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    public AnalysisDto generateAnalysis() {
        List<ViolationReport> reports = repository.findAll();
        
        if (reports.isEmpty()) {
            return createEmptyAnalysis();
        }

        Map<String, Long> violationsByState = reports.stream()
                .collect(Collectors.groupingBy(
                    report -> report.getViolationState().toString(),
                    Collectors.counting()
                ));

        Map<String, Long> violationsBySeverity = reports.stream()
                .collect(Collectors.groupingBy(
                    ViolationReport::getSeverity,
                    Collectors.counting()
                ));

        Map<String, Long> violationsByHour = reports.stream()
                .collect(Collectors.groupingBy(
                    report -> String.valueOf(report.getTimestamp().getHour()),
                    Collectors.counting()
                ));

        Map<String, Long> violationsByDay = reports.stream()
                .collect(Collectors.groupingBy(
                    report -> report.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    Collectors.counting()
                ));

        double averageViolationsPerDay = calculateAverageViolationsPerDay(reports);
        String mostCommonViolationType = findMostCommonViolationType(violationsByState);
        TrendAnalysis trends = analyzeTrends(reports);

        return new AnalysisDto(
            violationsByState,
            violationsBySeverity,
            violationsByHour,
            violationsByDay,
            averageViolationsPerDay,
            mostCommonViolationType,
            LocalDateTime.now(),
            reports.size(),
            trends
        );
    }

    private AnalysisDto createEmptyAnalysis() {
        return new AnalysisDto(
            Map.of(),
            Map.of(),
            Map.of(),
            Map.of(),
            0.0,
            "No violations",
            LocalDateTime.now(),
            0L,
            new TrendAnalysis("STABLE", 0.0, "No data available for trend analysis")
        );
    }

    private double calculateAverageViolationsPerDay(List<ViolationReport> reports) {
        if (reports.isEmpty()) return 0.0;
        
        Map<String, Long> dailyCounts = reports.stream()
                .collect(Collectors.groupingBy(
                    report -> report.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    Collectors.counting()
                ));
        
        return dailyCounts.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }

    private String findMostCommonViolationType(Map<String, Long> violationsByState) {
        return violationsByState.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No violations");
    }

    private TrendAnalysis analyzeTrends(List<ViolationReport> reports) {
        if (reports.size() < 2) {
            return new TrendAnalysis("STABLE", 0.0, "Insufficient data for trend analysis");
        }

        // Analyze last 7 days vs previous 7 days
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDateTime twoWeeksAgo = now.minusDays(14);

        long recentWeekCount = reports.stream()
                .filter(report -> report.getTimestamp().isAfter(weekAgo))
                .count();

        long previousWeekCount = reports.stream()
                .filter(report -> report.getTimestamp().isAfter(twoWeeksAgo) && 
                                report.getTimestamp().isBefore(weekAgo))
                .count();

        if (previousWeekCount == 0) {
            return new TrendAnalysis("INCREASING", 100.0, "New violations detected this week");
        }

        double changePercentage = ((double) (recentWeekCount - previousWeekCount) / previousWeekCount) * 100;
        
        String trend;
        String description;
        
        if (Math.abs(changePercentage) < 10) {
            trend = "STABLE";
            description = "Violation rates remain relatively stable";
        } else if (changePercentage > 0) {
            trend = "INCREASING";
            description = String.format("Violations increased by %.1f%% compared to previous week", changePercentage);
        } else {
            trend = "DECREASING";
            description = String.format("Violations decreased by %.1f%% compared to previous week", Math.abs(changePercentage));
        }

        return new TrendAnalysis(trend, changePercentage, description);
    }
}
