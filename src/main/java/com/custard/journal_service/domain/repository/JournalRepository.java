package com.custard.journal_service.domain.repository;

import com.custard.journal_service.domain.models.Journal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface JournalRepository {
    Mono<Journal> save(Journal journal);
    Mono<Journal> findById(String id);
    Flux<Journal> findByUserId(String userId);
    Mono<Void> deleteById(String id);
}

