package com.custard.journal_service.infrastructure.persistence.repository;

import com.custard.journal_service.domain.models.Transcript;
import com.custard.journal_service.domain.repository.TranscriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TranscriptRepositoryImpl implements TranscriptRepository {
    @Override
    public Mono<Transcript> save(Transcript transcript) {
        return null;
    }

    @Override
    public Mono<Transcript> findById(String id) {
        return null;
    }

    @Override
    public Flux<Transcript> findByJournalId(String journalId) {
        return null;
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return null;
    }
}
