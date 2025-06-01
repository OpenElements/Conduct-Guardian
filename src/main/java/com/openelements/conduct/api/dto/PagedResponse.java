package com.openelements.conduct.api.dto;

import org.jspecify.annotations.NonNull;

import java.util.List;

public record PagedResponse<T>(
    @NonNull List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {}
