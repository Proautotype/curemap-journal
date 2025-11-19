package com.custard.journal_service.domain.repository;

import com.custard.journal_service.domain.models.Transcript;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface TranscriptRepository {
    Mono<Transcript> save(Transcript transcript);

    Mono<Transcript> findById(String id);

    Flux<Transcript> findByJournalId(String journalId);

    Mono<Void> deleteById(String id);
}
