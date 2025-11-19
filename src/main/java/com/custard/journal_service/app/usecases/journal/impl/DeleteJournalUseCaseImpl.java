package com.custard.journal_service.app.usecases.journal.impl;

import com.custard.journal_service.app.commands.journal.DeleteJournalCommand;
import com.custard.journal_service.app.usecases.journal.DeleteJournalUseCase;
import com.custard.journal_service.domain.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DeleteJournalUseCaseImpl implements DeleteJournalUseCase {

    private final Logger logger = LoggerFactory.getLogger(DeleteJournalUseCaseImpl.class);
    private final JournalRepository journalRepository;

    @Override
    public Mono<Void> execute(DeleteJournalCommand command) {
        logger.info("deleting record with id {} ", command.journalId());
        return journalRepository.deleteById(command.journalId());
    }
}
