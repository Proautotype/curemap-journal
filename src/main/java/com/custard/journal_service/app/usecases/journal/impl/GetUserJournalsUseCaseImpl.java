package com.custard.journal_service.app.usecases.journal.impl;

import com.custard.journal_service.app.commands.journal.GetUserJournalsCommand;
import com.custard.journal_service.app.usecases.journal.GetUserJournalsUseCase;
import com.custard.journal_service.domain.models.Journal;
import com.custard.journal_service.domain.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class GetUserJournalsUseCaseImpl implements GetUserJournalsUseCase {

    private final Logger logger = LoggerFactory.getLogger(GetUserJournalsUseCaseImpl.class);
    private final JournalRepository journalRepository;

    @Override
    public Flux<Journal> execute(GetUserJournalsCommand command) {
        Flux<Journal> userJournals = journalRepository.findByUserId(command.userId());
        logger.info("found user journals {} ", command);
        return userJournals;
    }
}
