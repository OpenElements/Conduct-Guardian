package com.openelements.conduct.service;

import com.openelements.conduct.api.dto.PagedResponse;
import com.openelements.conduct.api.dto.ViolationReportDto;
import com.openelements.conduct.data.CheckResult;
import com.openelements.conduct.data.ViolationState;
import com.openelements.conduct.repository.ViolationReport;
import com.openelements.conduct.repository.ViolationReportRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ViolationReportService {

    private final ViolationReportRepository repository;

    @Autowired
    public ViolationReportService(@NonNull ViolationReportRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    public void saveReport(@NonNull CheckResult checkResult) {
        Objects.requireNonNull(checkResult, "checkResult must not be null");
        
        String title = checkResult.message().title() != null ? checkResult.message().title() : "No Title";
        String content = checkResult.message().message();
        
        ViolationReport report = new ViolationReport(
            title,
            content,
            checkResult.message().link(),
            checkResult.state(),
            checkResult.reason()
        );
        
        repository.save(report);
    }

    public PagedResponse<ViolationReportDto> getReports(int page, int size, String sortBy, String sortDir, 
                                                       ViolationState violationState, String severity,
                                                       LocalDateTime startDate, LocalDateTime endDate) {
        List<ViolationReport> allReports = repository.findAll();
        
        // Apply filters
        List<ViolationReport> filteredReports = allReports.stream()
                .filter(report -> violationState == null || report.getViolationState() == violationState)
                .filter(report -> severity == null || report.getSeverity().equals(severity))
                .filter(report -> startDate == null || !report.getTimestamp().isBefore(startDate))
                .filter(report -> endDate == null || !report.getTimestamp().isAfter(endDate))
                .collect(Collectors.toList());
        
        // Apply sorting
        Comparator<ViolationReport> comparator = getComparator(sortBy);
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        filteredReports.sort(comparator);
        
        // Apply pagination
        int totalElements = filteredReports.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        List<ViolationReport> pageContent = filteredReports.subList(start, end);
        List<ViolationReportDto> dtoContent = pageContent.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
            dtoContent,
            page,
            size,
            totalElements,
            totalPages,
            page == 0,
            page >= totalPages - 1
        );
    }

    public Optional<ViolationReportDto> getReportById(@NonNull String id) {
        return repository.findById(id)
                .map(this::convertToDto);
    }

    private Comparator<ViolationReport> getComparator(String sortBy) {
        return switch (sortBy) {
            case "timestamp" -> Comparator.comparing(ViolationReport::getTimestamp);
            case "severity" -> Comparator.comparing(ViolationReport::getSeverity);
            case "violationState" -> Comparator.comparing(ViolationReport::getViolationState);
            case "messageTitle" -> Comparator.comparing(ViolationReport::getMessageTitle);
            default -> Comparator.comparing(ViolationReport::getTimestamp);
        };
    }

    private ViolationReportDto convertToDto(ViolationReport report) {
        return new ViolationReportDto(
            report.getId(),
            report.getMessageTitle(),
            report.getMessageContent(),
            report.getMessageUrl(),
            report.getViolationState(),
            report.getReason(),
            report.getTimestamp(),
            report.getSeverity()
        );
    }
}
