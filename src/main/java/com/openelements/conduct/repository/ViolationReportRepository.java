package com.openelements.conduct.repository;

import com.openelements.conduct.data.ViolationState;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Repository
public class ViolationReportRepository {
    
    private static final int MAX_REPORTS = 1000;
    private final ConcurrentLinkedDeque<ViolationReport> reports = new ConcurrentLinkedDeque<>();

    public void save(@NonNull ViolationReport report) {
        Objects.requireNonNull(report, "report must not be null");
        
        synchronized (reports) {
            reports.addFirst(report);
            
            // Maintain size limit
            while (reports.size() > MAX_REPORTS) {
                reports.removeLast();
            }
        }
    }

    public List<ViolationReport> findAll() {
        return new ArrayList<>(reports);
    }

    public List<ViolationReport> findByViolationState(@NonNull ViolationState state) {
        return reports.stream()
                .filter(report -> report.getViolationState() == state)
                .collect(Collectors.toList());
    }

    public List<ViolationReport> findBySeverity(@NonNull String severity) {
        return reports.stream()
                .filter(report -> report.getSeverity().equals(severity))
                .collect(Collectors.toList());
    }

    public List<ViolationReport> findByDateRange(@NonNull LocalDateTime start, @NonNull LocalDateTime end) {
        return reports.stream()
                .filter(report -> !report.getTimestamp().isBefore(start) && !report.getTimestamp().isAfter(end))
                .collect(Collectors.toList());
    }

    public Optional<ViolationReport> findById(@NonNull String id) {
        return reports.stream()
                .filter(report -> report.getId().equals(id))
                .findFirst();
    }

    public long count() {
        return reports.size();
    }

    public void clear() {
        reports.clear();
    }
}
