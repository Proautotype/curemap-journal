package com.custard.journal_service.app.commands.transcript;

public record CreateTranscriptCommand(
        String journalId,
        String contentS3Key
) {
}
