package com.custard.journal_service.infrastructure.persistence.mapper;

import com.custard.journal_service.domain.models.Journal;
import com.custard.journal_service.infrastructure.persistence.model.JournalEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersistenceJournalMapper {

    private final ObjectMapper objectMapper;


    public JournalEntity toEntity(Journal journal) {
        try{
            JournalEntity entity = new JournalEntity();
            entity.setId(journal.getId());
            entity.setUserId(journal.getUserId());
            entity.setFileKey(journal.getContentS3Key());
            entity.setMetadata(objectMapper.writeValueAsString(journal.getContentMetadata()));
            return entity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Journal toModel(JournalEntity entity) {
        Journal journal = new Journal();
        journal.setId(entity.getId());
        journal.setUserId(entity.getUserId());
        journal.setContentS3Key(entity.getFileKey());
        return journal;
    }
}
