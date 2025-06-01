package com.openelements.conduct.repository;

import com.openelements.conduct.data.ViolationState;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class ViolationReport {
    private final String id;
    private final String messageTitle;
    private final String messageContent;
    private final URI messageUrl;
    private final ViolationState violationState;
    private final String reason;
    private final LocalDateTime timestamp;
    private final String severity;

    public ViolationReport(@NonNull String messageTitle, @NonNull String messageContent, 
                          @NonNull URI messageUrl, @NonNull ViolationState violationState, 
                          @NonNull String reason) {
        this.id = UUID.randomUUID().toString();
        this.messageTitle = Objects.requireNonNull(messageTitle, "messageTitle must not be null");
        this.messageContent = Objects.requireNonNull(messageContent, "messageContent must not be null");
        this.messageUrl = Objects.requireNonNull(messageUrl, "messageUrl must not be null");
        this.violationState = Objects.requireNonNull(violationState, "violationState must not be null");
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
        this.timestamp = LocalDateTime.now();
        this.severity = determineSeverity(violationState, reason);
    }

    private String determineSeverity(ViolationState state, String reason) {
        if (state == ViolationState.VIOLATION) {
            if (reason.toLowerCase().contains("severe") || reason.toLowerCase().contains("harassment")) {
                return "HIGH";
            } else if (reason.toLowerCase().contains("moderate") || reason.toLowerCase().contains("inappropriate")) {
                return "MEDIUM";
            } else {
                return "LOW";
            }
        }
        return "NONE";
    }

    // Getters
    public String getId() { return id; }
    public String getMessageTitle() { return messageTitle; }
    public String getMessageContent() { return messageContent; }
    public URI getMessageUrl() { return messageUrl; }
    public ViolationState getViolationState() { return violationState; }
    public String getReason() { return reason; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSeverity() { return severity; }
}
