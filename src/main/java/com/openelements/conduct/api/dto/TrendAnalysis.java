package com.openelements.conduct.api.dto;

import org.jspecify.annotations.NonNull;

public record TrendAnalysis(
    @NonNull String trend,
    double changePercentage,
    @NonNull String description
) {}
