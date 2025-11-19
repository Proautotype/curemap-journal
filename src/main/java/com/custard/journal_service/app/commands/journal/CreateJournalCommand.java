package com.custard.journal_service.app.commands.journal;

import java.util.Map;

public record CreateJournalCommand(
        String userId,
        String contentS3Key,
        Map<String, Object> metadata
) {}
