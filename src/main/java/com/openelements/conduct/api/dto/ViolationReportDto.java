package com.openelements.conduct.api.dto;

import com.openelements.conduct.data.ViolationState;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.time.LocalDateTime;

public record ViolationReportDto(
    @NonNull String id,
    @NonNull String messageTitle,
    @NonNull String messageContent,
    @NonNull URI messageUrl,
    @NonNull ViolationState violationState,
    @NonNull String reason,
    @NonNull LocalDateTime timestamp,
    @NonNull String severity
) {}
