package com.custard.journal_service.domain.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Transcript {
    private String id;
    private String journalId;
    private String content;
    private LocalDateTime createdAt;
}