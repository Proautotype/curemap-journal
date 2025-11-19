package com.custard.journal_service.infrastructure.persistence.mapper;

import com.custard.journal_service.domain.models.Journal;
import com.custard.journal_service.infrastructure.persistence.model.JournalEntity;
import org.springframework.stereotype.Component;

@Component
public class PersistenceJournalMapper {
    public JournalEntity toEntity(Journal journal) {
        JournalEntity entity = new JournalEntity();
        entity.setId(journal.getId());
        entity.setUserId(journal.getUserId());
        entity.setFileKey(journal.getContentS3Key());
        return entity;
    }

    public Journal toModel(JournalEntity entity) {
        Journal journal = new Journal();
        journal.setId(entity.getId());
        journal.setUserId(entity.getUserId());
        journal.setContentS3Key(entity.getFileKey());
        return journal;
    }
}
