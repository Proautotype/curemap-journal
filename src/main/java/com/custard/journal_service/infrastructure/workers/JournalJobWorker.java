package com.custard.journal_service.infrastructure.workers;

import com.custard.journal_service.app.commands.journal.CreateJournalCommand;
import com.custard.journal_service.app.usecases.journal.CreateJournalUseCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class JournalJobWorker {

    private final Logger logger = LoggerFactory.getLogger(JournalJobWorker.class);
    private final StringRedisTemplate redisTemplate;
    private final CreateJournalUseCase createJournalUseCase;
    // Add this field to the class
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService workerPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread();
        t.setDaemon(true);
        return t;
    });

    private static final String STREAM = "journal_jobs";
    private static final String GROUP = "journal_group";
    private static final String DLQ = "journal_jobs.DLQ";
    private static final int MAX_RETRIES = 3;

    @PostConstruct
    public void start() {
        workerPool.submit(this::runLoog);
    }

    private void runLoog() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream().read(
                        Consumer.from(GROUP, "worker-" + UUID.randomUUID()),
                        StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                        StreamOffset.create(STREAM, ReadOffset.lastConsumed())
                );


                if (messages == null || messages.isEmpty()) {
                    continue;
                }

                for (MapRecord<String, Object, Object> msg : messages) {
                    processRecord(msg);
                }

            } catch (Exception e) {
                logger.error("Worker loop error", e);
            }
        }
    }

    private void processRecord(MapRecord<String, Object, Object> record) {
        String id = record.getId().getValue();
        Map<Object, Object> vals = record.getValue();
        String fileKey = String.valueOf(vals.get("fileKey") != null ? vals.get("fileKey") : "");
        String userId = String.valueOf(vals.get("userId") != null ? vals.get("userId") : "");
        String metadataJson = String.valueOf(vals.get("metadata") != null ? vals.get("metadata") : "");

        String retryStr = vals.get("retry") != null ? String.valueOf(vals.get("retry")) : null;

        int retry = Integer.parseInt(Optional.ofNullable(retryStr).orElse("0"));

        try {

            // blocking DB call
            Map<String, Object> metaData = new HashMap<>();
            if (metadataJson != null || !metadataJson.isEmpty()) {
                metaData = objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {
                });
            }
            // blocking DB
            createJournalUseCase.execute(new CreateJournalCommand(userId, fileKey, metaData));

            // acknowledge processed message
            redisTemplate.opsForStream().acknowledge(STREAM, GROUP, id);
            logger.info("Processed job {} for file {}", id, fileKey);

        } catch (Exception e) {
            logger.error("Failed to process job {}, retry={}", id, retry, e);

            // Acknowledge the failed message (so it doesn't stay pending), then requeue or DLQ
            if (retry < MAX_RETRIES) {
                vals.put("retry", String.valueOf(retry + 1));
                // requeue with incremented retry
                redisTemplate.opsForStream().add(StreamRecords.mapBacked(vals).withStreamKey(STREAM));
                logger.info("Requeued message {} with retry {}", id, retry + 1);
            }
        }

    }

    @PreDestroy
    public void shutdown() {
        workerPool.shutdown();
    }


}
