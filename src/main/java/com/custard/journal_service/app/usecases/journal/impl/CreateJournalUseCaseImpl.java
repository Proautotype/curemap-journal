package com.custard.journal_service.app.usecases.journal.impl;

import com.custard.journal_service.app.commands.journal.CreateJournalCommand;
import com.custard.journal_service.app.usecases.journal.CreateJournalUseCase;
import com.custard.journal_service.app.mappers.JournalMapper;
import com.custard.journal_service.domain.models.Journal;
import com.custard.journal_service.domain.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CreateJournalUseCaseImpl implements CreateJournalUseCase {

    private final Logger logger = LoggerFactory.getLogger(CreateJournalUseCaseImpl.class);
    private final JournalRepository journalRepository;
    private final JournalMapper journalMapper;

    @Override
    public Mono<Journal> execute(CreateJournalCommand command) {
        logger.info("creating journal with key {} ", command.contentS3Key());
        Journal model = journalMapper.toModel(command);
        return journalRepository.save(model);
    }
}
