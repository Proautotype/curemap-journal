package com.custard.journal_service.infrastructure.persistence.model;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;


@Entity
@Table(name = "journals")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JournalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "file_key")
    private String fileKey;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
