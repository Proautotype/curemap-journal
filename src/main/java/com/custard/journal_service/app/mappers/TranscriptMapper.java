package com.custard.journal_service.app.mappers;

import com.custard.journal_service.app.commands.transcript.CreateTranscriptCommand;
import com.custard.journal_service.domain.models.Transcript;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TranscriptMapper {

    public Transcript toModel(CreateTranscriptCommand command) {
        Transcript transcript = new Transcript();
        transcript.setId(null);
        transcript.setJournalId(command.journalId());
        transcript.setContent(command.contentS3Key());
        transcript.setCreatedAt(LocalDateTime.now());
        return transcript;
    }

}
