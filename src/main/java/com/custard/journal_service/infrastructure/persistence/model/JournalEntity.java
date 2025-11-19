package com.custard.journal_service.infrastructure.persistence.model;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalEntity {
    @Id
    private String id;
    private String userId;
    private String fileKey;
    private String metadata;

    @CreatedDate
    private LocalDateTime createdAt;

    @PostConstruct
    private void init(){
        id = UUID.randomUUID().toString();
    }

}
