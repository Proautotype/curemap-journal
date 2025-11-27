package com.custard.journal_service.infrastructure.persistence.repository;

import com.custard.journal_service.domain.models.Journal;
import com.custard.journal_service.domain.repository.JournalRepository;
import com.custard.journal_service.infrastructure.persistence.mapper.PersistenceJournalMapper;
import com.custard.journal_service.infrastructure.persistence.model.JournalEntity;
import com.custard.journal_service.infrastructure.persistence.repository.jpa.JpaJournalRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalRepositoryImpl implements JournalRepository {

    private final PersistenceJournalMapper journalMapper;
    private final JpaJournalRepository journalRepository;
    private final Logger logger = LoggerFactory.getLogger(JournalRepositoryImpl.class);

    @Override
    public Mono<Journal> save(Journal journal) {
        JournalEntity entity = journalMapper.toEntity(journal);
        JournalEntity save = journalRepository.save(entity);
        Journal model = journalMapper.toModel(save);
        return Mono.just(model);
    }

    @Override
    public Mono<Journal> findById(String id) {
        JournalEntity journalEntity = journalRepository.findById(id).orElseThrow(() -> new RuntimeException(""));
        Journal model = journalMapper.toModel(journalEntity);
        return Mono.just(model);
    }

    @Override
    public Flux<Journal> findByUserId(String userId) {
        List<Journal> userJournals = journalRepository.findByUserIdIgnoreCase(userId)
                .stream()
                .map(journalMapper::toModel).toList();
        return Flux.fromIterable(userJournals);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        journalRepository.deleteById(id);
        return null;
    }
}
