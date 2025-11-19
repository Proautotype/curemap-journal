package com.custard.journal_service.infrastructure.persistence.repository.jpa;

import com.custard.journal_service.infrastructure.persistence.model.JournalEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface JpaJournalRepository extends ReactiveCrudRepository<JournalEntity, String> {
    Flux<JournalEntity> findByUserId(String userId);
}
