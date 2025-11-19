package com.custard.journal_service.infrastructure.persistence.repository;

import com.custard.journal_service.domain.models.Journal;
import com.custard.journal_service.domain.repository.JournalRepository;
import com.custard.journal_service.infrastructure.persistence.mapper.PersistenceJournalMapper;
import com.custard.journal_service.infrastructure.persistence.model.JournalEntity;
import com.custard.journal_service.infrastructure.persistence.repository.jpa.JpaJournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class JournalRepositoryImpl implements JournalRepository {

    private final PersistenceJournalMapper journalMapper;
    private final JpaJournalRepository jpaJournalRepository;

    @Override
    @Transactional
    public Mono<Journal> save(Journal journal) {
        JournalEntity entity = journalMapper.toEntity(journal);
        return jpaJournalRepository
                .save(entity)
                .map(journalMapper::toModel);
    }

    @Override
    public Mono<Journal> findById(String id) {
        return jpaJournalRepository
                .findById(id)
                .map(journalMapper::toModel);
    }

    @Override
    public Flux<Journal> findByUserId(String userId) {
        return jpaJournalRepository
                .findByUserId(userId)
                .map(journalMapper::toModel);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return jpaJournalRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Journal not found with id: " + id)))
                .flatMap(entity -> jpaJournalRepository.deleteById(id));
    }
}
