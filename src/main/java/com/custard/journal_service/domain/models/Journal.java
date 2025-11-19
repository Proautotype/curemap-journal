package com.custard.journal_service.domain.models;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class Journal {
    private String id;
    private String userId;
    private String contentS3Key;
    private Map<String, Object> contentMetadata;
    private LocalDateTime createdAt;
}