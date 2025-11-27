package com.custard.journal_service.infrastructure.persistence.repository.jpa;

import com.custard.journal_service.infrastructure.persistence.model.JournalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaJournalRepository extends JpaRepository<JournalEntity, String> {
    List<JournalEntity> findByUserIdIgnoreCase(String userId);
}
