package com.custard.journal_service.app.mappers;

import com.custard.journal_service.app.commands.journal.CreateJournalCommand;
import com.custard.journal_service.domain.models.Journal;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class JournalMapper {

    public Journal toModel(CreateJournalCommand command) {
        Journal journal = new Journal();
        journal.setId(null);
        journal.setUserId(command.userId());
        journal.setContentS3Key(command.contentS3Key());
        journal.setContentMetadata(command.metadata());
        journal.setCreatedAt(LocalDateTime.now());
        return journal;
    }

}
