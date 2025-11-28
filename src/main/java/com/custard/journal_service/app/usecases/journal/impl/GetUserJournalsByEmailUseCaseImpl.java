package com.custard.journal_service.app.usecases.journal.impl;

import com.custard.journal_service.app.commands.journal.GetUserJournalsCommand;
import com.custard.journal_service.app.exceptions.EntityNotFoundException;
import com.custard.journal_service.app.usecases.journal.GetUserJournalsByEmailUseCase;
import com.custard.journal_service.domain.models.Journal;
import com.custard.journal_service.domain.repository.JournalRepository;
import com.custard.journal_service.infrastructure.clients.AccountClient;
import com.custard.journal_service.infrastructure.clients.dto.SuccessApiResponse;
import com.custard.journal_service.infrastructure.clients.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetUserJournalsByEmailUseCaseImpl implements GetUserJournalsByEmailUseCase {

    private final AccountClient accountClient;
    private final JournalRepository journalRepository;

    @Override
    public Flux<Journal> execute(GetUserJournalsCommand command) {
        // userId represents email
        ResponseEntity<SuccessApiResponse<UserDto>> accountDetailsByEmail
                = accountClient.getAccountDetailsByEmail(command.userId());

        if (!accountDetailsByEmail.getStatusCode().is2xxSuccessful()) {
            throw new EntityNotFoundException(String.format("User with email %s not found", command.userId()));
        }

        UserDto data = accountDetailsByEmail.getBody().getData();

        return journalRepository.findByUserId(data.getId())
                .doOnComplete(() -> log.info("Successfully retrieved records for user: {}", data.getId()));
    }
}
