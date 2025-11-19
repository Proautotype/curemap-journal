package com.custard.journal_service.app.usecases.journal.impl;

import com.custard.journal_service.app.commands.journal.GetJournalByIdCommand;
import com.custard.journal_service.app.usecases.journal.GetJournalByIdUseCase;
import com.custard.journal_service.domain.models.Journal;
import com.custard.journal_service.domain.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetJournalByIdUseCaseImpl implements GetJournalByIdUseCase {
    private final Logger logger = LoggerFactory.getLogger(GetJournalByIdUseCaseImpl.class);
    private final JournalRepository journalRepository;
    @Override
    public Mono<Journal> execute(GetJournalByIdCommand command) {
        logger.info("Finding journal by id {} ", command);
        return journalRepository.findById(command.journalId());
    }
}
